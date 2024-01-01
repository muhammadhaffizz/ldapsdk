/*
 * Copyright 2022-2024 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright 2022-2024 Ping Identity Corporation
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
 * Copyright (C) 2022-2024 Ping Identity Corporation
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
package com.unboundid.ldap.sdk.unboundidds.logs.v2.json;



import com.unboundid.ldap.sdk.unboundidds.logs.AccessLogOperationType;
import com.unboundid.ldap.sdk.unboundidds.logs.LogException;
import com.unboundid.ldap.sdk.unboundidds.logs.v2.
            ExtendedRequestAccessLogMessage;
import com.unboundid.util.NotExtensible;
import com.unboundid.util.NotMutable;
import com.unboundid.util.NotNull;
import com.unboundid.util.Nullable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.json.JSONObject;



/**
 * This class provides a data structure that holds information about a
 * JSON-formatted extended request access log message.
 * <BR>
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class, and other classes within the
 *   {@code com.unboundid.ldap.sdk.unboundidds} package structure, are only
 *   supported for use against Ping Identity, UnboundID, and
 *   Nokia/Alcatel-Lucent 8661 server products.  These classes provide support
 *   for proprietary functionality or for external specifications that are not
 *   considered stable or mature enough to be guaranteed to work in an
 *   interoperable way with other types of LDAP servers.
 * </BLOCKQUOTE>
 */
@NotExtensible()
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public class JSONExtendedRequestAccessLogMessage
       extends JSONRequestAccessLogMessage
       implements ExtendedRequestAccessLogMessage
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -1086842370470161550L;



  // The OID for the extended request.
  @Nullable private final String requestOID;

  // The name for the extended request.
  @Nullable private final String requestType;



  /**
   * Creates a new JSON extended request access log message from the provided
   * JSON object.
   *
   * @param  jsonObject  The JSON object that contains an encoded representation
   *                     of this log message.  It must not be {@code null}.
   *
   * @throws  LogException  If the provided JSON object cannot be parsed as a
   *                        valid log message.
   */
  public JSONExtendedRequestAccessLogMessage(
              @NotNull final JSONObject jsonObject)
         throws LogException
  {
    super(jsonObject);

    requestOID = getString(JSONFormattedAccessLogFields.EXTENDED_REQUEST_OID);
    requestType = getString(JSONFormattedAccessLogFields.EXTENDED_REQUEST_TYPE);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @NotNull()
  public final AccessLogOperationType getOperationType()
  {
    return AccessLogOperationType.EXTENDED;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @Nullable()
  public final String getRequestOID()
  {
    return requestOID;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @Nullable()
  public final String getRequestType()
  {
    return requestType;
  }
}
