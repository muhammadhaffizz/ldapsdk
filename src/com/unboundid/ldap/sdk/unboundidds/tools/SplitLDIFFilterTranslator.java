/*
 * Copyright 2016-2017 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2016-2017 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.tools;



import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;
import com.unboundid.util.Debug;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.tools.ToolMessages.*;



/**
 * This class provides an implementation of an LDIF reader entry translator that
 * can be used to determine the set into which an entry should be placed by
 * selecting the first set for which the associated filter matches the entry.
 * <BR>
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 */
@ThreadSafety(level=ThreadSafetyLevel.NOT_THREADSAFE)
final class SplitLDIFFilterTranslator
      extends SplitLDIFTranslator
{
  // The map used to cache decisions made by this translator.
  private final ConcurrentHashMap<String,Set<String>> rdnCache;

  // A map used to associate the search filter for each set with the name of
  // that set.
  private final Map<Filter,Set<String>> setFilters;

  // A map of the names that will be used for each of the sets.
  private final Map<Integer,Set<String>> setNames;

  // The schema to use for filter evaluation.
  private final Schema schema;

  // The sets in which entries outside the split base should be placed.
  private final Set<String> outsideSplitBaseSetNames;

  // The sets in which the split base entry should be placed.
  private final Set<String> splitBaseEntrySetNames;



  /**
   * Creates a new instance of this translator with the provided information.
   *
   * @param  splitBaseDN                           The base DN below which to
   *                                               split entries.
   * @param  schema                                The schema to use for filter
   *                                               evaluation.
   * @param  filters                               The filters to use to select
   *                                               the appropriate backend set.
   *                                               This must not be
   *                                               {@code null} or empty.
   * @param  assumeFlatDIT                         Indicates whether to assume
   *                                               that the DIT is flat, and
   *                                               there aren't any entries more
   *                                               than one level below the
   *                                               split base DN.  If this is
   *                                               {@code true}, then any
   *                                               entries more than one level
   *                                               below the split base DN will
   *                                               be considered an error.
   * @param  addEntriesOutsideSplitToAllSets       Indicates whether entries
   *                                               outside the split should be
   *                                               added to all sets.
   * @param  addEntriesOutsideSplitToDedicatedSet  Indicates whether entries
   *                                               outside the split should be
   *                                               added to all sets.
   */
  SplitLDIFFilterTranslator(final DN splitBaseDN, final Schema schema,
                            final LinkedHashSet<Filter> filters,
                            final boolean assumeFlatDIT,
                            final boolean addEntriesOutsideSplitToAllSets,
                            final boolean addEntriesOutsideSplitToDedicatedSet)
  {
    super(splitBaseDN);

    this.schema = schema;

    if (assumeFlatDIT)
    {
      rdnCache = null;
    }
    else
    {
      rdnCache = new ConcurrentHashMap<String,Set<String>>(100);
    }

    final int numSets = filters.size();
    outsideSplitBaseSetNames = new LinkedHashSet<String>(numSets+1);
    splitBaseEntrySetNames = new LinkedHashSet<String>(numSets);

    if (addEntriesOutsideSplitToDedicatedSet)
    {
      outsideSplitBaseSetNames.add(SplitLDIFEntry.SET_NAME_OUTSIDE_SPLIT);
    }

    setFilters = new LinkedHashMap<Filter,Set<String>>(numSets);
    setNames = new LinkedHashMap<Integer,Set<String>>(numSets);

    int i=0;
    for (final Filter f : filters)
    {
      final String setName = ".set" + (i+1);
      final Set<String> sets = Collections.singleton(setName);

      splitBaseEntrySetNames.add(setName);

      if (addEntriesOutsideSplitToAllSets)
      {
        outsideSplitBaseSetNames.add(setName);
      }

      setFilters.put(f, sets);
      setNames.put(i, sets);

      i++;
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public SplitLDIFEntry translate(final Entry original,
                                  final long firstLineNumber)
         throws LDIFException
  {
    // Get the parsed DN for the entry.  If we can't, that's an error and we
    // should only include it in the error set.
    final DN dn;
    try
    {
      dn = original.getParsedDN();
    }
    catch (final LDAPException le)
    {
      Debug.debugException(le);
      return createEntry(original,
           ERR_SPLIT_LDIF_FILTER_TRANSLATOR_CANNOT_PARSE_DN.get(
                le.getMessage()),
           getErrorSetNames());
    }


    // If the parsed DN is outside the split base DN, then return the
    // appropriate sets for that.
    if (! dn.isDescendantOf(getSplitBaseDN(), true))
    {
      return createEntry(original, outsideSplitBaseSetNames);
    }


    // If the parsed DN matches the split base DN, then it will always go into
    // all of the split sets.
    if (dn.equals(getSplitBaseDN()))
    {
      return createEntry(original, splitBaseEntrySetNames);
    }


    // Determine which RDN component is immediately below the split base DN.
    final RDN[] rdns = dn.getRDNs();
    final int targetRDNIndex = rdns.length - getSplitBaseRDNs().length - 1;
    final String normalizedRDNString =
         rdns[targetRDNIndex].toNormalizedString();


    // If the target RDN component is not the first component of the DN, then
    // we'll use the cache to send this entry to the same set as its parent.
    if (targetRDNIndex > 0)
    {
      // If we aren't maintaining an RDN cache (which should only happen if
      // the --assumeFlatDIT argument was provided), then this is an error.
      if (rdnCache == null)
      {
        return createEntry(original,
             ERR_SPLIT_LDIF_FILTER_TRANSLATOR_NON_FLAT_DIT.get(
                  getSplitBaseDN().toString()),
             getErrorSetNames());
      }

      // Note that even if we are maintaining an RDN cache, it may not contain
      // the information that we need to determine which set should hold this
      // entry.  There are two reasons for this:
      //
      // - The LDIF file contains an entry below the split base DN without
      //   including the parent for that entry, (or includes a child entry
      //   before its parent).
      //
      // - We are processing multiple entries in parallel, and the parent entry
      //   is currently being processed in another thread and that thread hasn't
      //   yet made the determination as to which set should be used for that
      //   parent entry.
      //
      // In either case, use null for the target set names.  If we are in the
      // parallel processing phase, then we will re-invoke this method later
      // at a point in which we can be confident that the caching should have
      // been performed  If we still get null the second time through, then
      // the caller will consider that an error and handle it appropriately.
      return createEntry(original, rdnCache.get(normalizedRDNString));
    }


    // At this point, we know that the entry is exactly one level below the
    // split base DN.  Iterate through the filters and see if any of them
    // matches the entry.
    for (final Map.Entry<Filter,Set<String>> e : setFilters.entrySet())
    {
      final Filter f = e.getKey();
      try
      {
        if (f.matchesEntry(original, schema))
        {
          final Set<String> sets = e.getValue();
          if (rdnCache != null)
          {
            rdnCache.put(normalizedRDNString, sets);
          }

          return createEntry(original, sets);
        }
      }
      catch (final Exception ex)
      {
        Debug.debugException(ex);
      }
    }


    // If we've gotten here, then none of the filters matched so pick a set
    // based on a hash of the RDN.
    final SplitLDIFEntry e = createFromRDNHash(original, dn, setNames);
    if (rdnCache != null)
    {
      rdnCache.put(normalizedRDNString, e.getSets());
    }

    return e;
  }
}