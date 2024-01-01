/*
 * Copyright 2012-2024 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright 2012-2024 Ping Identity Corporation
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
 * Copyright (C) 2012-2024 Ping Identity Corporation
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
package com.unboundid.ldap.sdk.unboundidds;



import java.util.ArrayList;
import java.util.List;

import com.unboundid.asn1.ASN1Element;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.asn1.ASN1Sequence;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.ToCodeArgHelper;
import com.unboundid.ldap.sdk.ToCodeHelper;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.NotNull;
import com.unboundid.util.Nullable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.Validator;

import static com.unboundid.ldap.sdk.unboundidds.UnboundIDDSMessages.*;



/**
 * This class provides an implementation of the UNBOUNDID-TOTP SASL bind request
 * that contains a point-in-time version of the one-time password and can be
 * used for a single bind but is not suitable for repeated use.  This version of
 * the bind request should be used for authentication in which the one-time
 * password is provided by an external source rather than being generated by
 * the LDAP SDK.
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
 * <BR>
 * Because the one-time password is provided rather than generated, this version
 * of the bind request is not suitable for cases in which the authentication
 * process may need to be repeated (e.g., for use in a connection pool,
 * following referrals, or if the auto-reconnect feature is enabled), then the
 * reusable variant (supported by the {@link ReusableTOTPBindRequest} class)
 * which generates the one-time password should be used instead.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.NOT_THREADSAFE)
public final class SingleUseTOTPBindRequest
       extends UnboundIDTOTPBindRequest
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -4429898810534930296L;



  // The hard-coded TOTP password to include in the bind request.
  @NotNull private final String totpPassword;



  /**
   * Creates a new SASL TOTP bind request with the provided information.
   *
   * @param  authenticationID  The authentication identity for the bind request.
   *                           It must not be {@code null}, and must be in the
   *                           form "u:" followed by a username, or "dn:"
   *                           followed by a DN.
   * @param  authorizationID   The authorization identity for the bind request.
   *                           It may be {@code null} if the authorization
   *                           identity should be the same as the authentication
   *                           identity.  If an authorization identity is
   *                           specified, it must be in the form "u:" followed
   *                           by a username, or "dn:" followed by a DN.  The
   *                           value "dn:" may indicate an authorization
   *                           identity of the anonymous user.
   * @param  totpPassword      The hard-coded TOTP password to include in the
   *                           bind request.  It must not be {@code null}.
   * @param  staticPassword    The static password for the target user.  It may
   *                           be {@code null} if only the one-time password is
   *                           to be used for authentication (which may or may
   *                           not be allowed by the server).
   * @param  controls          The set of controls to include in the bind
   *                           request.
   */
  public SingleUseTOTPBindRequest(@NotNull final String authenticationID,
                                  @Nullable final String authorizationID,
                                  @NotNull final String totpPassword,
                                  @Nullable final String staticPassword,
                                  @Nullable final Control... controls)
  {
    super(authenticationID, authorizationID, staticPassword, controls);

    Validator.ensureNotNull(totpPassword);
    this.totpPassword = totpPassword;
  }



  /**
   * Creates a new SASL TOTP bind request with the provided information.
   *
   * @param  authenticationID  The authentication identity for the bind request.
   *                           It must not be {@code null}, and must be in the
   *                           form "u:" followed by a username, or "dn:"
   *                           followed by a DN.
   * @param  authorizationID   The authorization identity for the bind request.
   *                           It may be {@code null} if the authorization
   *                           identity should be the same as the authentication
   *                           identity.  If an authorization identity is
   *                           specified, it must be in the form "u:" followed
   *                           by a username, or "dn:" followed by a DN.  The
   *                           value "dn:" may indicate an authorization
   *                           identity of the anonymous user.
   * @param  totpPassword      The hard-coded TOTP password to include in the
   *                           bind request.  It must not be {@code null}.
   * @param  staticPassword    The static password for the target user.  It may
   *                           be {@code null} if only the one-time password is
   *                           to be used for authentication (which may or may
   *                           not be allowed by the server).
   * @param  controls          The set of controls to include in the bind
   *                           request.
   */
  public SingleUseTOTPBindRequest(@NotNull final String authenticationID,
                                  @Nullable final String authorizationID,
                                  @NotNull final String totpPassword,
                                  @Nullable final byte[] staticPassword,
                                  @Nullable final Control... controls)
  {
    super(authenticationID, authorizationID, staticPassword, controls);

    Validator.ensureNotNull(totpPassword);
    this.totpPassword = totpPassword;
  }



  /**
   * Creates a new SASL TOTP bind request with the provided information.
   *
   * @param  authenticationID  The authentication identity for the bind request.
   *                           It must not be {@code null}, and must be in the
   *                           form "u:" followed by a username, or "dn:"
   *                           followed by a DN.
   * @param  authorizationID   The authorization identity for the bind request.
   *                           It may be {@code null} if the authorization
   *                           identity should be the same as the authentication
   *                           identity.  If an authorization identity is
   *                           specified, it must be in the form "u:" followed
   *                           by a username, or "dn:" followed by a DN.  The
   *                           value "dn:" may indicate an authorization
   *                           identity of the anonymous user.
   * @param  totpPassword      The hard-coded TOTP password to include in the
   *                           bind request.  It must not be {@code null}.
   * @param  staticPassword    The static password for the target user.  It may
   *                           be {@code null} if only the one-time password is
   *                           to be used for authentication (which may or may
   *                           not be allowed by the server).
   * @param  controls          The set of controls to include in the bind
   *                           request.
   */
  private SingleUseTOTPBindRequest(@NotNull final String authenticationID,
               @Nullable final String authorizationID,
               @NotNull final String totpPassword,
               @Nullable final ASN1OctetString staticPassword,
               @Nullable final Control... controls)
  {
    super(authenticationID, authorizationID, staticPassword, controls);

    Validator.ensureNotNull(totpPassword);
    this.totpPassword = totpPassword;
  }



  /**
   * Creates a new single-use TOTP bind request from the information contained
   * in the provided encoded SASL credentials.
   *
   * @param  saslCredentials  The encoded SASL credentials to be decoded in
   *                          order to create this single-use TOTP bind request.
   *                          It must not be {@code null}.
   * @param  controls         The set of controls to include in the bind
   *                          request.
   *
   * @return  The single-use TOTP bind request decoded from the provided
   *          credentials.
   *
   * @throws  LDAPException  If the provided credentials are not valid for an
   *                         UNBOUNDID-TOTP bind request.
   */
  @NotNull()
  public static SingleUseTOTPBindRequest decodeSASLCredentials(
                     @NotNull final ASN1OctetString saslCredentials,
                     @Nullable final Control... controls)
         throws LDAPException
  {
    try
    {
      String          authenticationID = null;
      String          authorizationID  = null;
      String          totpPassword     = null;
      ASN1OctetString staticPassword   = null;

      final ASN1Sequence s =
           ASN1Sequence.decodeAsSequence(saslCredentials.getValue());
      for (final ASN1Element e : s.elements())
      {
        switch (e.getType())
        {
          case TYPE_AUTHENTICATION_ID:
            authenticationID = e.decodeAsOctetString().stringValue();
            break;
          case TYPE_AUTHORIZATION_ID:
            authorizationID = e.decodeAsOctetString().stringValue();
            break;
          case TYPE_TOTP_PASSWORD:
            totpPassword = e.decodeAsOctetString().stringValue();
            break;
          case TYPE_STATIC_PASSWORD:
            staticPassword = e.decodeAsOctetString();
            break;
          default:
            throw new LDAPException(ResultCode.DECODING_ERROR,
                 ERR_SINGLE_USE_TOTP_DECODE_INVALID_ELEMENT_TYPE.get(
                      StaticUtils.toHex(e.getType())));
        }
      }

      if (authenticationID == null)
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_SINGLE_USE_TOTP_DECODE_MISSING_AUTHN_ID.get());
      }

      if (totpPassword == null)
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_SINGLE_USE_TOTP_DECODE_MISSING_TOTP_PW.get());
      }

      return new SingleUseTOTPBindRequest(authenticationID, authorizationID,
           totpPassword, staticPassword, controls);
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_SINGLE_USE_TOTP_DECODE_ERROR.get(
                StaticUtils.getExceptionMessage(e)),
           e);
    }
  }



  /**
   * Retrieves the hard-coded TOTP password to include in the bind request.
   *
   * @return  The hard-coded TOTP password to include in the bind request.
   */
  @NotNull()
  public String getTOTPPassword()
  {
    return totpPassword;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @NotNull()
  protected ASN1OctetString getSASLCredentials()
  {
    return encodeCredentials(getAuthenticationID(), getAuthorizationID(),
         totpPassword, getStaticPassword());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @Nullable()
  public SingleUseTOTPBindRequest getRebindRequest(@NotNull final String host,
                                                   final int port)
  {
    // Automatic rebinding is not supported for single-use TOTP binds.
    return null;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @NotNull()
  public SingleUseTOTPBindRequest duplicate()
  {
    return duplicate(getControls());
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @NotNull()
  public SingleUseTOTPBindRequest duplicate(@Nullable final Control[] controls)
  {
    final SingleUseTOTPBindRequest bindRequest =
         new SingleUseTOTPBindRequest(getAuthenticationID(),
              getAuthorizationID(), totpPassword, getStaticPassword(),
              controls);
    bindRequest.setResponseTimeoutMillis(getResponseTimeoutMillis(null));
    bindRequest.setIntermediateResponseListener(
         getIntermediateResponseListener());
    bindRequest.setReferralDepth(getReferralDepth());
    bindRequest.setReferralConnector(getReferralConnectorInternal());
    return bindRequest;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toCode(@NotNull final List<String> lineList,
                     @NotNull final String requestID,
                     final int indentSpaces, final boolean includeProcessing)
  {
    // Create the request variable.
    final ArrayList<ToCodeArgHelper> constructorArgs = new ArrayList<>(5);
    constructorArgs.add(ToCodeArgHelper.createString(getAuthenticationID(),
         "Authentication ID"));
    constructorArgs.add(ToCodeArgHelper.createString(getAuthorizationID(),
         "Authorization ID"));
    constructorArgs.add(ToCodeArgHelper.createString(
         "---redacted-totp-password---", "TOTP Password"));
    constructorArgs.add(ToCodeArgHelper.createString(
         ((getStaticPassword() == null)
              ? "null"
              : "---redacted-static-password---"),
         "Static Password"));

    final Control[] controls = getControls();
    if (controls.length > 0)
    {
      constructorArgs.add(ToCodeArgHelper.createControlArray(controls,
           "Bind Controls"));
    }

    ToCodeHelper.generateMethodCall(lineList, indentSpaces,
         "SingleUseTOTPBindRequest", requestID + "Request",
         "new SingleUseTOTPBindRequest", constructorArgs);


    // Add lines for processing the request and obtaining the result.
    if (includeProcessing)
    {
      // Generate a string with the appropriate indent.
      final StringBuilder buffer = new StringBuilder();
      for (int i=0; i < indentSpaces; i++)
      {
        buffer.append(' ');
      }
      final String indent = buffer.toString();

      lineList.add("");
      lineList.add(indent + "try");
      lineList.add(indent + '{');
      lineList.add(indent + "  BindResult " + requestID +
           "Result = connection.bind(" + requestID + "Request);");
      lineList.add(indent + "  // The bind was processed successfully.");
      lineList.add(indent + '}');
      lineList.add(indent + "catch (LDAPException e)");
      lineList.add(indent + '{');
      lineList.add(indent + "  // The bind failed.  Maybe the following will " +
           "help explain why.");
      lineList.add(indent + "  // Note that the connection is now likely in " +
           "an unauthenticated state.");
      lineList.add(indent + "  ResultCode resultCode = e.getResultCode();");
      lineList.add(indent + "  String message = e.getMessage();");
      lineList.add(indent + "  String matchedDN = e.getMatchedDN();");
      lineList.add(indent + "  String[] referralURLs = e.getReferralURLs();");
      lineList.add(indent + "  Control[] responseControls = " +
           "e.getResponseControls();");
      lineList.add(indent + '}');
    }
  }
}
