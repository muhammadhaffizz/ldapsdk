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
package com.unboundid.ldap.sdk.transformations;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldif.LDIFAddChangeRecord;
import com.unboundid.ldif.LDIFChangeRecord;
import com.unboundid.ldif.LDIFDeleteChangeRecord;
import com.unboundid.ldif.LDIFModifyChangeRecord;
import com.unboundid.ldif.LDIFModifyDNChangeRecord;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * This class provides an implementation of an entry and LDIF change record
 * transformation that will alter DNs at or below a specified base DN to replace
 * that base DN with a different base DN.  This replacement will be applied to
 * the DNs of entries that are transformed, as well as in any attribute values
 * that represent DNs at or below the specified base DN.
 */
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class MoveSubtreeTransformation
       implements EntryTransformation, LDIFChangeRecordTransformation
{
  // The source base DN to be replaced.
  private final DN sourceDN;

  // A list of the RDNs in the target base DN.
  private final List<RDN> targetRDNs;



  /**
   * Creates a new move subtree transformation with the provided information.
   *
   * @param  sourceDN  The source base DN to be replaced with the target base
   *                   DN.  It must not be {@code null}.
   * @param  targetDN  The target base DN to use to replace the source base DN.
   *                   It must not be {@code null}.
   */
  public MoveSubtreeTransformation(final DN sourceDN, final DN targetDN)
  {
    this.sourceDN = sourceDN;

    targetRDNs = Arrays.asList(targetDN.getRDNs());
  }



  /**
   * {@inheritDoc}
   */
  public Entry transformEntry(final Entry e)
  {
    if (e == null)
    {
      return null;
    }


    // Iterate through the attributes in the entry and make any appropriate DN
    // replacements
    final Collection<Attribute> originalAttributes = e.getAttributes();
    final ArrayList<Attribute> newAttributes =
         new ArrayList<Attribute>(originalAttributes.size());
    for (final Attribute a : originalAttributes)
    {
      final String[] originalValues = a.getValues();
      final String[] newValues = new String[originalValues.length];
      for (int i=0; i < originalValues.length; i++)
      {
        newValues[i] = processString(originalValues[i]);
      }

      newAttributes.add(new Attribute(a.getName(), newValues));
    }

    return new Entry(processString(e.getDN()), newAttributes);
  }



  /**
   * {@inheritDoc}
   */
  public LDIFChangeRecord transformChangeRecord(final LDIFChangeRecord r)
  {
    if (r == null)
    {
      return null;
    }


    if (r instanceof LDIFAddChangeRecord)
    {
      // Just use the same processing as for an entry.
      final LDIFAddChangeRecord addRecord = (LDIFAddChangeRecord) r;
      return new LDIFAddChangeRecord(transformEntry(addRecord.getEntryToAdd()),
           addRecord.getControls());
    }
    if (r instanceof LDIFDeleteChangeRecord)
    {
      return new LDIFDeleteChangeRecord(processString(r.getDN()),
           r.getControls());
    }
    else if (r instanceof LDIFModifyChangeRecord)
    {
      final LDIFModifyChangeRecord modRecord = (LDIFModifyChangeRecord) r;
      final Modification[] originalMods = modRecord.getModifications();
      final Modification[] newMods = new Modification[originalMods.length];
      for (int i=0; i < originalMods.length; i++)
      {
        final Modification m = originalMods[i];
        if (m.hasValue())
        {
          final String[] originalValues = m.getValues();
          final String[] newValues = new String[originalValues.length];
          for (int j=0; j < originalValues.length; j++)
          {
            newValues[j] = processString(originalValues[j]);
          }
          newMods[i] = new Modification(m.getModificationType(),
               m.getAttributeName(), newValues);
        }
        else
        {
          newMods[i] = originalMods[i];
        }
      }

      return new LDIFModifyChangeRecord(processString(modRecord.getDN()),
           newMods, modRecord.getControls());
    }
    else if (r instanceof LDIFModifyDNChangeRecord)
    {
      final LDIFModifyDNChangeRecord modDNRecord = (LDIFModifyDNChangeRecord) r;
      return new LDIFModifyDNChangeRecord(processString(modDNRecord.getDN()),
           modDNRecord.getNewRDN(), modDNRecord.deleteOldRDN(),
           processString(modDNRecord.getNewSuperiorDN()),
           modDNRecord.getControls());
    }
    else
    {
      // This should never happen.
      return r;
    }
  }



  /**
   * Identifies whether the provided string represents a DN that is at or below
   * the specified source base DN.  If so, then it will be updated to replace
   * the old base DN with the new base DN.  Otherwise, the original string will
   * be returned.
   *
   * @param  s  The string to process.
   *
   * @return  A new string if the provided value was a valid DN at or below the
   *          source DN, or the original string if it was not a valid DN or was
   *          not below the source DN.
   */
  String processString(final String s)
  {
    if (s == null)
    {
      return null;
    }

    try
    {
      final DN dn = new DN(s);
      if (! dn.isDescendantOf(sourceDN, true))
      {
        return s;
      }

      final RDN[] originalRDNs = dn.getRDNs();
      final RDN[] sourceRDNs = sourceDN.getRDNs();
      final ArrayList<RDN> newRDNs = new ArrayList<RDN>(2*originalRDNs.length);
      final int numComponentsToKeep = originalRDNs.length - sourceRDNs.length;
      for (int i=0; i < numComponentsToKeep; i++)
      {
        newRDNs.add(originalRDNs[i]);
      }

      newRDNs.addAll(targetRDNs);
      return new DN(newRDNs).toString();
    }
    catch (final Exception e)
    {
      // This is fine.  The value isn't a DN.
      return s;
    }
  }



  /**
   * {@inheritDoc}
   */
  public Entry translate(final Entry original, final long firstLineNumber)
  {
    return transformEntry(original);
  }



  /**
   * {@inheritDoc}
   */
  public LDIFChangeRecord translate(final LDIFChangeRecord original,
                                    final long firstLineNumber)
  {
    return transformChangeRecord(original);
  }



  /**
   * {@inheritDoc}
   */
  public Entry translateEntryToWrite(final Entry original)
  {
    return transformEntry(original);
  }



  /**
   * {@inheritDoc}
   */
  public LDIFChangeRecord translateChangeRecordToWrite(
                               final LDIFChangeRecord original)
  {
    return transformChangeRecord(original);
  }
}