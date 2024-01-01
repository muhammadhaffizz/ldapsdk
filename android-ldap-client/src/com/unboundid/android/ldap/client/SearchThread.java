/*
 * Copyright 2009-2024 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright 2009-2024 Ping Identity Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2009-2024 Ping Identity Corporation
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
package com.unboundid.android.ldap.client;



import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;

import static com.unboundid.android.ldap.client.Logger.*;



/**
 * This class defines a thread that will be used to perform a search and
 * provide the result to the activity that started it.
 */
final class SearchThread
      extends Thread
{
  /**
   * The size limit that will be used for searches.
   */
  static final int SIZE_LIMIT = 100;



  /**
   * The time limit (in seconds) that will be used for searches.
   */
  static final int TIME_LIMIT_SECONDS = 30;



  /**
   * The tag that will be used for log messages generated by this class.
   */
  private static final String LOG_TAG = "SearchThread";



  // The filter to use for the search.
  private final Filter filter;

  // The activity that created this thread.
  private final SearchServer caller;

  // The instance in which the search is to be performed.
  private final ServerInstance instance;



  /**
   * Creates a new search thread with the provided information.
   *
   * @param  caller    The activity that created this thread.
   * @param  instance  The instance in which the search is to be performed.
   * @param  filter    The filter to use for the search.
   */
  SearchThread(final SearchServer caller, final ServerInstance instance,
               final Filter filter)
  {
    logEnter(LOG_TAG, "<init>", caller, instance, filter);

    this.caller   = caller;
    this.instance = instance;
    this.filter   = filter;
  }



  /**
   * Processes the search and returns the result to the caller.
   */
  @Override()
  public void run()
  {
    logEnter(LOG_TAG, "run");

    // Perform the search.
    SearchResult result;
    LDAPConnection conn = null;
    try
    {
      conn = instance.getConnection(caller);

      final SearchRequest request = new SearchRequest(instance.getBaseDN(),
           SearchScope.SUB, filter);
      request.setSizeLimit(SIZE_LIMIT);
      request.setTimeLimitSeconds(TIME_LIMIT_SECONDS);

      result = conn.search(request);
    }
    catch (final LDAPSearchException lse)
    {
      logException(LOG_TAG, "run", lse);

      result = lse.getSearchResult();
    }
    catch (final LDAPException le)
    {
      logException(LOG_TAG, "run", le);

      result = new LDAPSearchException(le).getSearchResult();
    }
    finally
    {
      if (conn != null)
      {
        conn.close();
      }
    }

    caller.searchDone(result);
  }
}
