/*
 * Copyright 2007-2024 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright 2007-2024 Ping Identity Corporation
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
 * Copyright (C) 2007-2024 Ping Identity Corporation
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
package com.unboundid.ldap.sdk.controls;



import org.testng.annotations.Test;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.LDAPSDKTestCase;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.Base64;
import com.unboundid.util.LDAPSDKUsageException;
import com.unboundid.util.json.JSONField;
import com.unboundid.util.json.JSONObject;



/**
 * This class provides a set of test cases for the
 * ProxiedAuthorizationV1RequestControl class.
 */
public class ProxiedAuthorizationV1RequestControlTestCase
       extends LDAPSDKTestCase
{
  /**
   * Tests the first constructor with a non-empty DN.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testConstructor1()
         throws Exception
  {
    String proxyDN = "uid=test.user,ou=People,dc=example,dc=com";
    ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(proxyDN);
    c = new ProxiedAuthorizationV1RequestControl(c);

    assertNotNull(c.getProxyDN());
    assertEquals(c.getProxyDN(), proxyDN);

    assertTrue(c.isCritical());

    assertNotNull(c.getControlName());
    assertNotNull(c.toString());
  }



  /**
   * Tests the first constructor with an empty DN.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testConstructor1EmptyDN()
         throws Exception
  {
    String proxyDN = "";
    ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(proxyDN);
    c = new ProxiedAuthorizationV1RequestControl(c);

    assertNotNull(c.getProxyDN());
    assertEquals(c.getProxyDN(), proxyDN);

    assertTrue(c.isCritical());

    assertNotNull(c.getControlName());
    assertNotNull(c.toString());
  }



  /**
   * Tests the first constructor with a {@code null} DN.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(expectedExceptions = { LDAPSDKUsageException.class,
                               NullPointerException.class })
  public void testConstructor1NullDN()
         throws Exception
  {
    new ProxiedAuthorizationV1RequestControl((String) null);
  }



  /**
   * Tests the second constructor with a non-empty DN.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testConstructor2()
         throws Exception
  {
    String proxyDN = "uid=test.user,ou=People,dc=example,dc=com";
    ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(new DN(proxyDN));
    c = new ProxiedAuthorizationV1RequestControl(c);

    assertNotNull(c.getProxyDN());
    assertEquals(c.getProxyDN(), proxyDN);

    assertTrue(c.isCritical());

    assertNotNull(c.getControlName());
    assertNotNull(c.toString());
  }



  /**
   * Tests the second constructor with an empty DN.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testConstructor2EmptyDN()
         throws Exception
  {
    String proxyDN = "";
    ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(new DN(proxyDN));
    c = new ProxiedAuthorizationV1RequestControl(c);

    assertNotNull(c.getProxyDN());
    assertEquals(c.getProxyDN(), proxyDN);

    assertTrue(c.isCritical());

    assertNotNull(c.getControlName());
    assertNotNull(c.toString());
  }



  /**
   * Tests the second constructor with a {@code null} DN.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(expectedExceptions = { LDAPSDKUsageException.class,
                               NullPointerException.class })
  public void testConstructor2NullDN()
         throws Exception
  {
    new ProxiedAuthorizationV1RequestControl((DN) null);
  }



  /**
   * Tests the third constructor with a generic control with no value.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(expectedExceptions = { LDAPException.class })
  public void testConstructor3NoValue()
         throws Exception
  {
    Control c = new Control(ProxiedAuthorizationV1RequestControl.
                                 PROXIED_AUTHORIZATION_V1_REQUEST_OID,
                            true, null);
    new ProxiedAuthorizationV1RequestControl(c);
  }



  /**
   * Tests the third constructor with a generic control with an invalid value.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(expectedExceptions = { LDAPException.class })
  public void testConstructor3InvalidValue()
         throws Exception
  {
    Control c = new Control(ProxiedAuthorizationV1RequestControl.
                                 PROXIED_AUTHORIZATION_V1_REQUEST_OID,
                            true, new ASN1OctetString("foo"));
    new ProxiedAuthorizationV1RequestControl(c);
  }



  /**
   * Sends a search request to the server with a proxied auth v1 control with an
   * appropriately-authorized user.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testSendRequestWithAuthorizedUser()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    LDAPConnection conn = getAdminConnection();
    conn.add(getTestBaseDN(), getBaseEntryAttributes());

    String proxyDN = "uid=proxy.user," + getTestBaseDN();
    Attribute[] proxyUserAttrs =
    {
      new Attribute("objectClass", "top", "person", "organizationalPerson",
                    "inetOrgPerson"),
      new Attribute("uid", "proxy.user"),
      new Attribute("givenName", "Proxy"),
      new Attribute("sn", "User"),
      new Attribute("cn", "Proxy User"),
      new Attribute("userPassword", "password"),
      new Attribute("ds-privilege-name", "bypass-acl", "proxied-auth")
    };
    conn.add(proxyDN, proxyUserAttrs);

    String targetDN = "uid=target.user," + getTestBaseDN();
    Attribute[] targetUserAttrs =
    {
      new Attribute("objectClass", "top", "person", "organizationalPerson",
                    "inetOrgPerson"),
      new Attribute("uid", "target.user"),
      new Attribute("givenName", "Target"),
      new Attribute("sn", "User"),
      new Attribute("cn", "Target User"),
      new Attribute("userPassword", "password")
    };
    conn.add(targetDN, targetUserAttrs);

    conn.bind(proxyDN, "password");

    SearchRequest searchRequest =
         new SearchRequest("dc=example,dc=com", SearchScope.BASE,
                           "(objectClass=*)");
    searchRequest.addControl(
         new ProxiedAuthorizationV1RequestControl(targetDN));

    SearchResult searchResult = conn.search(searchRequest);
    assertEquals(searchResult.getResultCode(), ResultCode.SUCCESS);

    conn.bind(getTestBindDN(), getTestBindPassword());

    conn.delete(targetDN);
    conn.delete(proxyDN);
    conn.delete(getTestBaseDN());
    conn.close();
  }



  /**
   * Sends a search request to the server with a proxied auth v1 control with an
   * inappropriately-authorized user.
   * <BR><BR>
   * Access to a Directory Server instance is required for complete processing.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testSendRequestWithUnauthorizedUser()
         throws Exception
  {
    if (! isDirectoryInstanceAvailable())
    {
      return;
    }

    LDAPConnection conn = getAdminConnection();
    conn.add(getTestBaseDN(), getBaseEntryAttributes());

    String proxyDN = "uid=proxy.user," + getTestBaseDN();
    Attribute[] proxyUserAttrs =
    {
      new Attribute("objectClass", "top", "person", "organizationalPerson",
                    "inetOrgPerson"),
      new Attribute("uid", "proxy.user"),
      new Attribute("givenName", "Proxy"),
      new Attribute("sn", "User"),
      new Attribute("cn", "Proxy User"),
      new Attribute("userPassword", "password"),
      new Attribute("ds-privilege-name", "bypass-acl")
    };
    conn.add(proxyDN, proxyUserAttrs);

    String targetDN = "uid=target.user," + getTestBaseDN();
    Attribute[] targetUserAttrs =
    {
      new Attribute("objectClass", "top", "person", "organizationalPerson",
                    "inetOrgPerson"),
      new Attribute("uid", "target.user"),
      new Attribute("givenName", "Target"),
      new Attribute("sn", "User"),
      new Attribute("cn", "Target User"),
      new Attribute("userPassword", "password")
    };
    conn.add(targetDN, targetUserAttrs);

    conn.bind(proxyDN, "password");

    SearchRequest searchRequest =
         new SearchRequest("dc=example,dc=com", SearchScope.BASE,
                           "(objectClass=*)");
    searchRequest.addControl(
         new ProxiedAuthorizationV1RequestControl(targetDN));

    try
    {
      conn.search(searchRequest);
      fail("Expected authorization denied result when searching with the " +
            "proxied auth v1 control.");
    }
    catch (LDAPSearchException lse)
    {
      assertEquals(lse.getResultCode(), ResultCode.AUTHORIZATION_DENIED);
    }

    conn.bind(getTestBindDN(), getTestBindPassword());

    conn.delete(targetDN);
    conn.delete(proxyDN);
    conn.delete(getTestBaseDN());
    conn.close();
  }



  /**
   * Tests the behavior when trying to encode and decode the control to and
   * from a JSON object.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testToJSONControl()
          throws Exception
  {
    final ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(
              "uid=jdoe,ou=People,dc=example,dc=com");

    final JSONObject controlObject = c.toJSONControl();

    assertNotNull(controlObject);
    assertEquals(controlObject.getFields().size(), 4);

    assertEquals(controlObject.getFieldAsString("oid"), c.getOID());

    assertNotNull(controlObject.getFieldAsString("control-name"));
    assertFalse(controlObject.getFieldAsString("control-name").isEmpty());
    assertFalse(controlObject.getFieldAsString("control-name").equals(
         controlObject.getFieldAsString("oid")));

    assertEquals(controlObject.getFieldAsBoolean("criticality"),
         Boolean.TRUE);

    assertFalse(controlObject.hasField("value-base64"));

    assertEquals(controlObject.getFieldAsObject("value-json"),
         new JSONObject(
              new JSONField("authorization-dn",
                   "uid=jdoe,ou=People,dc=example,dc=com")));


    ProxiedAuthorizationV1RequestControl decodedControl =
         ProxiedAuthorizationV1RequestControl.decodeJSONControl(controlObject,
              true);
    assertNotNull(decodedControl);

    assertEquals(decodedControl.getOID(), c.getOID());

    assertTrue(decodedControl.isCritical());

    assertNotNull(decodedControl.getValue());

    assertEquals(decodedControl.getProxyDN(),
         "uid=jdoe,ou=People,dc=example,dc=com");


    decodedControl =
         (ProxiedAuthorizationV1RequestControl)
         Control.decodeJSONControl(controlObject, true, true);
    assertNotNull(decodedControl);

    assertEquals(decodedControl.getOID(), c.getOID());

    assertTrue(decodedControl.isCritical());

    assertNotNull(decodedControl.getValue());

    assertEquals(decodedControl.getProxyDN(),
         "uid=jdoe,ou=People,dc=example,dc=com");
  }



  /**
   * Tests the behavior when trying to decode a JSON object as a control when
   * the value is base64-encoded.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testDecodeJSONControlValueBase64()
          throws Exception
  {
    final ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(
              "uid=jdoe,ou=People,dc=example,dc=com");

    final JSONObject controlObject = new JSONObject(
         new JSONField("oid", c.getOID()),
         new JSONField("criticality", c.isCritical()),
         new JSONField("value-base64", Base64.encode(c.getValue().getValue())));


    ProxiedAuthorizationV1RequestControl decodedControl =
         ProxiedAuthorizationV1RequestControl.decodeJSONControl(controlObject,
              true);
    assertNotNull(decodedControl);

    assertEquals(decodedControl.getOID(), c.getOID());

    assertTrue(decodedControl.isCritical());

    assertNotNull(decodedControl.getValue());

    assertEquals(decodedControl.getProxyDN(),
         "uid=jdoe,ou=People,dc=example,dc=com");


    decodedControl =
         (ProxiedAuthorizationV1RequestControl)
         Control.decodeJSONControl(controlObject, true, true);
    assertNotNull(decodedControl);

    assertEquals(decodedControl.getOID(), c.getOID());

    assertTrue(decodedControl.isCritical());

    assertNotNull(decodedControl.getValue());

    assertEquals(decodedControl.getProxyDN(),
         "uid=jdoe,ou=People,dc=example,dc=com");
  }



  /**
   * Tests the behavior when trying to decode a JSON object as a control when
   * the value is missing the required authorization ND field.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(expectedExceptions = { LDAPException.class })
  public void testDecodeJSONControlValueMissingAuthorizationDN()
          throws Exception
  {
    final ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(
              "uid=jdoe,ou=People,dc=example,dc=com");

    final JSONObject controlObject = new JSONObject(
         new JSONField("oid", c.getOID()),
         new JSONField("criticality", c.isCritical()),
         new JSONField("value-json", JSONObject.EMPTY_OBJECT));


    ProxiedAuthorizationV1RequestControl.decodeJSONControl(controlObject, true);
  }



  /**
   * Tests the behavior when trying to decode a JSON object as a control when
   * the value has an unrecognized field in strict mode.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test(expectedExceptions = { LDAPException.class })
  public void testDecodeJSONControlValueUnrecognizedFieldStrict()
          throws Exception
  {
    final ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(
              "uid=jdoe,ou=People,dc=example,dc=com");

    final JSONObject controlObject = new JSONObject(
         new JSONField("oid", c.getOID()),
         new JSONField("criticality", c.isCritical()),
         new JSONField("value-json", new JSONObject(
              new JSONField("authorization-dn",
                   "uid=jdoe,ou=People,dc=example,dc=com"),
              new JSONField("unrecognized", "foo"))));


    ProxiedAuthorizationV1RequestControl.decodeJSONControl(controlObject, true);
  }



  /**
   * Tests the behavior when trying to decode a JSON object as a control when
   * the value has an unrecognized field in non-strict mode.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testDecodeJSONControlValueUnrecognizedFieldNonStrict()
          throws Exception
  {
    final ProxiedAuthorizationV1RequestControl c =
         new ProxiedAuthorizationV1RequestControl(
              "uid=jdoe,ou=People,dc=example,dc=com");

    final JSONObject controlObject = new JSONObject(
         new JSONField("oid", c.getOID()),
         new JSONField("criticality", c.isCritical()),
         new JSONField("value-json", new JSONObject(
              new JSONField("authorization-dn",
                   "uid=jdoe,ou=People,dc=example,dc=com"),
              new JSONField("unrecognized", "foo"))));


    ProxiedAuthorizationV1RequestControl decodedControl =
         ProxiedAuthorizationV1RequestControl.decodeJSONControl(controlObject,
              false);
    assertNotNull(decodedControl);

    assertEquals(decodedControl.getOID(), c.getOID());

    assertTrue(decodedControl.isCritical());

    assertNotNull(decodedControl.getValue());

    assertEquals(decodedControl.getProxyDN(),
         "uid=jdoe,ou=People,dc=example,dc=com");


    decodedControl =
         (ProxiedAuthorizationV1RequestControl)
         Control.decodeJSONControl(controlObject, false, true);
    assertNotNull(decodedControl);

    assertEquals(decodedControl.getOID(), c.getOID());

    assertTrue(decodedControl.isCritical());

    assertNotNull(decodedControl.getValue());

    assertEquals(decodedControl.getProxyDN(),
         "uid=jdoe,ou=People,dc=example,dc=com");
  }
}
