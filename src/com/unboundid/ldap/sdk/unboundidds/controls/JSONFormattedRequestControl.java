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
package com.unboundid.ldap.sdk.unboundidds.controls;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.JSONControlDecodeHelper;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.NotNull;
import com.unboundid.util.Nullable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.json.JSONArray;
import com.unboundid.util.json.JSONField;
import com.unboundid.util.json.JSONObject;
import com.unboundid.util.json.JSONValue;

import static com.unboundid.ldap.sdk.unboundidds.controls.ControlMessages.*;



/**
 * This class provides an implementation of a request control that may be used
 * to encapsulate a set of zero or more other controls represented as JSON
 * objects, and to indicate that the server should return any response controls
 * in a {@link JSONFormattedResponseControl}.
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
 * This control has an OID of 1.3.6.1.4.1.30221.2.5.64, and it may optionally
 * take a value.  If the control is provided without a value, then it merely
 * indicates that the server should return response controls in a
 * {@code JSONFormattedResponseControl}.  If the control has a value, then that
 * value should be a JSON object that contains a single field,
 * {@code controls}, whose value is an array of the JSON representations of the
 * request controls that should be sent to the server.  The JSON representations
 * of the controls is the one generated by the {@link Control#toJSONControl()}
 * method, and is the one expected by the {@link Control#decodeJSONControl}
 * method.  In particular, each control should have at least an {@code oid}
 * field that specifies the OID for the control, and a {@code criticality} field
 * that indicates whether the control is considered critical.  If the control
 * has a value, then either the {@code value-base64} field should be used to
 * provide a base64-encoded representation of the value, or
 * the {@code value-json} field should be used to provide a JSON-formatted
 * representation of the value for controls that support it.
 * <BR><BR>
 * The criticality for this control should generally be {@code true}, especially
 * if it embeds any controls with a criticality of {@code true}.  Any controls
 * embedded in the value of this control will be processed by the server with
 * the criticality of that embedded control.
 *
 * @see  JSONFormattedResponseControl
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class JSONFormattedRequestControl
       extends Control
{
  /**
   * The OID (1.3.6.1.4.1.30221.2.5.64) for the JSON-formatted request control.
   */
  @NotNull public static final  String JSON_FORMATTED_REQUEST_OID =
       "1.3.6.1.4.1.30221.2.5.64";



  /**
   * The name of the field used to hold the array of embedded controls in the
   * JSON representation of this control.
   */
  @NotNull private static final String JSON_FIELD_CONTROLS = "controls";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -1165320564468120423L;



  // A JSON object with an encoded representation of the value for this control.
  @Nullable private final JSONObject encodedValue;

  // A list of the JSON objects representing embedded controls within this
  // request control.
  @NotNull private final List<JSONObject> controlObjects;



  /**
   * Creates a new instance of this control with the specified criticality and
   * set of controls.
   *
   * @param  isCritical      Indicates whether the control should be considered
   *                         critical.  This should generally be {@code true},
   *                         although it is acceptable for it to be
   *                         {@code false} if there are no embedded controls,
   *                         or if all of the embedded controls have a
   *                         criticality of {@code false}.
   * @param  encodedValue    A JSON object with an encoded representation of
   *                         the value for this control.  It may be
   *                         {@code null} if the control should not have a
   *                         value.
   * @param  controlObjects  A collection of JSON objects representing the
   *                         controls to include in the request.  It must not
   *                         be {@code null}, but may be empty.
   */
  private JSONFormattedRequestControl(final boolean isCritical,
               @Nullable final JSONObject encodedValue,
               @NotNull final List<JSONObject> controlObjects)
  {
    super(JSON_FORMATTED_REQUEST_OID, isCritical,
         ((encodedValue == null)
              ? null :
              new ASN1OctetString(encodedValue.toSingleLineString())));

    this.encodedValue = encodedValue;
    this.controlObjects = controlObjects;
  }



  /**
   * Creates a new {@code JSONFormattedRequestControl} without any embedded
   * controls.  This may be used to indicate that no request controls are
   * needed, but the server should return any response controls in a
   * {@link JSONFormattedResponseControl}.
   *
   * @param  isCritical  Indicates whether this control should be considered
   *                     critical.
   *
   * @return  The {@code JSONFormattedRequestControl} that was created.
   */
  @NotNull()
  public static JSONFormattedRequestControl createEmptyControl(
              final boolean isCritical)
  {
    return new JSONFormattedRequestControl(isCritical, null,
         Collections.<JSONObject>emptyList());
  }



  /**
   * Creates a new {@code JSONFormattedRequestControl} with the provided set of
   * embedded controls.
   *
   * @param  isCritical  Indicates whether the control should be considered
   *                     critical.  This should generally be {@code true},
   *                     although it is acceptable for it to be {@code false} if
   *                     there are no embedded controls, or if all of the
   *                     embedded controls have a criticality of {@code false}.
   * @param  controls    The collection of controls to embed within this
   *                     request control.  This may be {@code null} or empty if
   *                     the request should not have any embedded controls.
   *
   * @return  The {@code JSONFormattedRequestControl} that was created.
   */
  @NotNull()
  public static JSONFormattedRequestControl createWithControls(
              final boolean isCritical,
              @Nullable final Control... controls)
  {
    return createWithControls(isCritical, StaticUtils.toList(controls));
  }



  /**
   * Creates a new {@code JSONFormattedRequestControl} with the provided set of
   * embedded controls.
   *
   * @param  isCritical  Indicates whether the control should be considered
   *                     critical.  This should generally be {@code true},
   *                     although it is acceptable for it to be {@code false} if
   *                     there are no embedded controls, or if all of the
   *                     embedded controls have a criticality of {@code false}.
   * @param  controls    The collection of controls to embed within this
   *                     request control.  This may be {@code null} or empty if
   *                     the request should not have any embedded controls.
   *
   * @return  The {@code JSONFormattedRequestControl} that was created.
   */
  @NotNull()
  public static JSONFormattedRequestControl createWithControls(
              final boolean isCritical,
              @Nullable final Collection<Control> controls)
  {
    if ((controls == null) || controls.isEmpty())
    {
      return new JSONFormattedRequestControl(isCritical, null,
           Collections.<JSONObject>emptyList());
    }


    final List<JSONObject> controlObjects = new ArrayList<>(controls.size());
    for (final Control c : controls)
    {
      controlObjects.add(c.toJSONControl());
    }

    final JSONObject encodedValue = new JSONObject(
         new JSONField(JSON_FIELD_CONTROLS, new JSONArray(controlObjects)));

    return new JSONFormattedRequestControl(isCritical, encodedValue,
         Collections.unmodifiableList(controlObjects));
  }



  /**
   * Creates a new {@code JSONFormattedRequestControl} with the provided set of
   * embedded JSON objects.
   *
   * @param  isCritical      Indicates whether the control should be considered
   *                         critical.  This should generally be {@code true},
   *                         although it is acceptable for it to be
   *                         {@code false} if there are no embedded controls, or
   *                         if all of the embedded controls have a criticality
   *                         of {@code false}.
   * @param  controlObjects  The collection of JSON objects that represent the
   *                         encoded controls to embed within this request
   *                         control.  This may be {@code null} or empty if the
   *                         request should not have any embedded controls.
   *                         Note that no attempt will be made to validate the
   *                         JSON objects as controls.
   *
   * @return  The {@code JSONFormattedRequestControl} that was created.
   */
  @NotNull()
  public static JSONFormattedRequestControl createWithControlObjects(
              final boolean isCritical,
              @Nullable final JSONObject... controlObjects)
  {
    return createWithControlObjects(isCritical,
         StaticUtils.toList(controlObjects));
  }



  /**
   * Creates a new {@code JSONFormattedRequestControl} with the provided set of
   * embedded JSON objects.
   *
   * @param  isCritical      Indicates whether the control should be considered
   *                         critical.  This should generally be {@code true},
   *                         although it is acceptable for it to be
   *                         {@code false} if there are no embedded controls, or
   *                         if all of the embedded controls have a criticality
   *                         of {@code false}.
   * @param  controlObjects  The collection of JSON objects that represent the
   *                         encoded controls to embed within this request.
   *                         This may be {@code null} or empty if the request
   *                         control should not have any embedded controls.
   *                         Note that no attempt will be made to validate the
   *                         JSON objects as controls.
   *
   * @return  The {@code JSONFormattedRequestControl} that was created.
   */
  @NotNull()
  public static JSONFormattedRequestControl createWithControlObjects(
              final boolean isCritical,
              @Nullable final Collection<JSONObject> controlObjects)
  {
    if ((controlObjects == null) || controlObjects.isEmpty())
    {
      return new JSONFormattedRequestControl(isCritical, null,
           Collections.<JSONObject>emptyList());
    }


    final List<JSONObject> controlObjectList = new ArrayList<>(controlObjects);
    final JSONObject encodedValue = new JSONObject(
         new JSONField(JSON_FIELD_CONTROLS, new JSONArray(controlObjectList)));

    return new JSONFormattedRequestControl(isCritical, encodedValue,
         Collections.unmodifiableList(controlObjectList));
  }



  /**
   * Creates a new instance of this control that is decoded from the provided
   * generic control.  Note that if the provided control has a value, it will be
   * validated to ensure that it is a JSON object containing only a
   * {@code controls} field whose value is an array of JSON objects that appear
   * to be well-formed generic JSON controls, but it will not make any attempt
   * to validate in a control-specific manner.
   *
   * @param  control  The control to decode as a JSON-formatted request control.
   *
   * @throws LDAPException  If a problem is encountered while attempting to
   *                         decode the provided control as a JSON-formatted
   *                         request control.
   */
  public JSONFormattedRequestControl(@NotNull final Control control)
         throws LDAPException
  {
    super(control);

    final ASN1OctetString rawValue = control.getValue();
    if (rawValue == null)
    {
      encodedValue = null;
      controlObjects = Collections.emptyList();
      return;
    }

    try
    {
      encodedValue = new JSONObject(rawValue.stringValue());
    }
    catch (final Exception e)
    {
      Debug.debugException(e);
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_JSON_FORMATTED_REQUEST_VALUE_NOT_JSON.get(), e);
    }


    final List<String> unrecognizedFields =
           JSONControlDecodeHelper.getControlObjectUnexpectedFields(
                encodedValue, JSON_FIELD_CONTROLS);
    if (! unrecognizedFields.isEmpty())
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_JSON_FORMATTED_REQUEST_UNRECOGNIZED_FIELD.get(
                unrecognizedFields.get(0)));
    }


    final List<JSONValue> controlValues =
         encodedValue.getFieldAsArray(JSON_FIELD_CONTROLS);
    if (controlValues == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_JSON_FORMATTED_REQUEST_VALUE_MISSING_CONTROLS.get(
                JSON_FIELD_CONTROLS));
    }

    if (controlValues.isEmpty())
    {
      controlObjects = Collections.emptyList();
      return;
    }

    final List<JSONObject> controlObjectsList =
         new ArrayList<>(controlValues.size());
    for (final JSONValue controlValue : controlValues)
    {
      if (controlValue instanceof JSONObject)
      {
        final JSONObject embeddedControlObject = (JSONObject) controlValue;

        try
        {
          new JSONControlDecodeHelper(embeddedControlObject, true, true, false);
          controlObjectsList.add(embeddedControlObject);
        }
        catch (final LDAPException e)
        {
          Debug.debugException(e);
          throw new LDAPException(ResultCode.DECODING_ERROR,
               ERR_JSON_FORMATTED_REQUEST_VALUE_NOT_CONTROL.get(
                    JSON_FIELD_CONTROLS,
                    embeddedControlObject.toSingleLineString(),
                    e.getMessage()),
               e);
        }
      }
      else
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_JSON_FORMATTED_REQUEST_VALUE_CONTROL_NOT_OBJECT.get(
                  JSON_FIELD_CONTROLS));
      }
    }


    controlObjects = Collections.unmodifiableList(controlObjectsList);
  }



  /**
   * Retrieves a list of the JSON objects that represent the embedded request
   * controls.  These JSON objects may not have been validated to ensure that
   * they represent valid controls.
   *
   * @return  A list of the JSON objects that represent the embedded request
   *          controls.  It may be empty if there are no embedded request
   *          controls.
   */
  @NotNull()
  public List<JSONObject> getControlObjects()
  {
    return controlObjects;
  }



  /**
   * Attempts to retrieve a decoded representation of the embedded request
   * controls using the specified behavior.
   *
   * @param  behavior                The behavior to use when parsing JSON
   *                                 objects as controls.  It must not be
   *                                 {@code null}.
   * @param  nonFatalDecodeMessages  An optional list that may be updated with
   *                                 messages about any JSON objects that could
   *                                 not be parsed as valid controls, but that
   *                                 should not result in an exception as per
   *                                 the provided behavior.  This may be
   *                                 {@code null} if no such messages are
   *                                 desired.  If it is non-{@code null}, then
   *                                 the list must be updatable.
   *
   * @return  A decoded representation of the embedded request controls, or an
   *          empty list if there are no embedded request controls or if none of
   *          the embedded JSON objects can be parsed as valid controls but that
   *          should not result in an exception as per the provided behavior.
   *
   * @throws  LDAPException  If any of the JSON objects cannot be parsed as a
   *                         valid control
   */
  @NotNull()
  public synchronized List<Control> decodeEmbeddedControls(
              @NotNull final JSONFormattedControlDecodeBehavior behavior,
              @Nullable final List<String> nonFatalDecodeMessages)
         throws LDAPException
  {
    // Iterate through the controls and try to decode them.
    final List<Control> controlList = new ArrayList<>(controlObjects.size());
    final List<String> fatalMessages = new ArrayList<>(controlObjects.size());
    for (final JSONObject controlObject : controlObjects)
    {
      // First, try to decode the JSON object as a generic control without any
      // specific decoding based on its OID.
      final JSONControlDecodeHelper jsonControl;
      try
      {
        jsonControl = new JSONControlDecodeHelper(controlObject,
             behavior.strict(), true, false);
      }
      catch (final LDAPException e)
      {
        Debug.debugException(e);

        if (behavior.throwOnUnparsableObject())
        {
          fatalMessages.add(e.getMessage());
        }
        else if (nonFatalDecodeMessages != null)
        {
          nonFatalDecodeMessages.add(e.getMessage());
        }

        continue;
      }


      // If the control is itself an embedded JSON-formatted request control,
      // see how we should handle it.
      if (jsonControl.getOID().equals(JSON_FORMATTED_REQUEST_OID))
      {
        if (! behavior.allowEmbeddedJSONFormattedControl())
        {
          final String message =
               ERR_JSON_FORMATTED_REQUEST_DISALLOWED_EMBEDDED_CONTROL.get();
          if (jsonControl.getCriticality())
          {
            fatalMessages.add(message);
          }
          else if (nonFatalDecodeMessages != null)
          {
            nonFatalDecodeMessages.add(message);
          }

          continue;
        }
      }


      // Try to actually decode the JSON object as a control, potentially using
      // control-specific logic based on its OID.
      try
      {
        controlList.add(Control.decodeJSONControl(controlObject,
             behavior.strict(), true));
      }
      catch (final LDAPException e)
      {
        Debug.debugException(e);

        if (jsonControl.getCriticality())
        {
          if (behavior.throwOnInvalidCriticalControl())
          {
            fatalMessages.add(e.getMessage());
          }
          else if (nonFatalDecodeMessages != null)
          {
            nonFatalDecodeMessages.add(e.getMessage());
          }
        }
        else
        {
          if (behavior.throwOnInvalidNonCriticalControl())
          {
            fatalMessages.add(e.getMessage());
          }
          else if (nonFatalDecodeMessages != null)
          {
            nonFatalDecodeMessages.add(e.getMessage());
          }
        }
      }
    }


    //  If there are any fatal messages, then we'll throw an exception with
    //  them.
    if (! fatalMessages.isEmpty())
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           StaticUtils.concatenateStrings(fatalMessages));
    }


    return Collections.unmodifiableList(controlList);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  @NotNull()
  public String getControlName()
  {
    return INFO_CONTROL_NAME_JSON_FORMATTED_REQUEST.get();
  }



  /**
   * Retrieves a representation of this JSON-formatted request control as a JSON
   * object.  The JSON object uses the following fields:
   * <UL>
   *   <LI>
   *     {@code oid} -- A mandatory string field whose value is the object
   *     identifier for this control.  For the JSON-formatted request control,
   *     the OID is "1.3.6.1.4.1.30221.2.5.64".
   *   </LI>
   *   <LI>
   *     {@code control-name} -- An optional string field whose value is a
   *     human-readable name for this control.  This field is only intended for
   *     descriptive purposes, and when decoding a control, the {@code oid}
   *     field should be used to identify the type of control.
   *   </LI>
   *   <LI>
   *     {@code criticality} -- A mandatory Boolean field used to indicate
   *     whether this control is considered critical.
   *   </LI>
   *   <LI>
   *     {@code value-base64} -- An optional string field whose value is a
   *     base64-encoded representation of the raw value for this JSON-formatted
   *     request control.  At most one of the {@code value-base64} and
   *     {@code value-json} fields must be present.
   *   </LI>
   *   <LI>
   *     {@code value-json} -- An optional JSON object field whose value is a
   *     user-friendly representation of the value for this JSON-formatted
   *     request control.  At most one of the {@code value-base64} and
   *     {@code value-json} fields must be present, and if the
   *     {@code value-json} field is used, then it will use the following
   *     fields:
   *     <UL>
   *       <LI>
   *         {@code controls} -- An mandatory array field whose values are JSON
   *         objects that represent the JSON-formatted request controls to send
   *         to the server.
   *       </LI>
   *     </UL>
   *   </LI>
   * </UL>
   *
   * @return  A JSON object that contains a representation of this control.
   */
  @Override()
  @NotNull()
  public JSONObject toJSONControl()
  {
    if (encodedValue == null)
    {
      return new JSONObject(
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_OID,
                JSON_FORMATTED_REQUEST_OID),
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_CONTROL_NAME,
                INFO_CONTROL_NAME_JSON_FORMATTED_REQUEST.get()),
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_CRITICALITY,
                isCritical()));
    }
    else
    {
      return new JSONObject(
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_OID,
                JSON_FORMATTED_REQUEST_OID),
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_CONTROL_NAME,
                INFO_CONTROL_NAME_JSON_FORMATTED_REQUEST.get()),
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_CRITICALITY,
                isCritical()),
           new JSONField(JSONControlDecodeHelper.JSON_FIELD_VALUE_JSON,
                encodedValue));
    }
  }



  /**
   * Attempts to decode the provided object as a JSON representation of a
   * JSON-formatted request control.
   *
   * @param  controlObject  The JSON object to be decoded.  It must not be
   *                        {@code null}.
   * @param  strict         Indicates whether to use strict mode when decoding
   *                        the provided JSON object.  If this is {@code true},
   *                        then this method will throw an exception if the
   *                        provided JSON object contains any unrecognized
   *                        fields.  If this is {@code false}, then unrecognized
   *                        fields will be ignored.
   *
   * @return  The JSON-formatted request control that was decoded from the
   *          provided JSON object.
   *
   * @throws  LDAPException  If the provided JSON object cannot be parsed as a
   *                         valid JSON-formatted request control.
   */
  @NotNull()
  public static JSONFormattedRequestControl decodeJSONControl(
              @NotNull final JSONObject controlObject,
              final boolean strict)
         throws LDAPException
  {
    final JSONControlDecodeHelper jsonControl = new JSONControlDecodeHelper(
         controlObject, strict, true, false);

    final ASN1OctetString rawValue = jsonControl.getRawValue();
    if (rawValue != null)
    {
      return new JSONFormattedRequestControl(new Control(
           jsonControl.getOID(), jsonControl.getCriticality(), rawValue));
    }


    final JSONObject valueObject = jsonControl.getValueObject();
    if (valueObject == null)
    {
      return new JSONFormattedRequestControl(jsonControl.getCriticality(), null,
           Collections.<JSONObject>emptyList());
    }


    final List<JSONValue> controlValues =
         valueObject.getFieldAsArray(JSON_FIELD_CONTROLS);
    if (controlValues == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_JSON_FORMATTED_REQUEST_DECODE_VALUE_MISSING_CONTROLS.get(
                controlObject.toSingleLineString(), JSON_FIELD_CONTROLS));
    }

    if (controlValues.isEmpty())
    {
      return new JSONFormattedRequestControl(jsonControl.getCriticality(),
           valueObject, Collections.<JSONObject>emptyList());
    }

    final List<JSONObject> controlObjectsList =
         new ArrayList<>(controlValues.size());
    for (final JSONValue controlValue : controlValues)
    {
      if (controlValue instanceof JSONObject)
      {
        final JSONObject embeddedControlObject = (JSONObject) controlValue;

        try
        {
          new JSONControlDecodeHelper(embeddedControlObject, strict, true,
               false);
          controlObjectsList.add(embeddedControlObject);
        }
        catch (final LDAPException e)
        {
          Debug.debugException(e);
          throw new LDAPException(ResultCode.DECODING_ERROR,
               ERR_JSON_FORMATTED_REQUEST_DECODE_VALUE_NOT_CONTROL.get(
                    controlObject.toSingleLineString(), JSON_FIELD_CONTROLS,
                    embeddedControlObject.toSingleLineString(), e.getMessage()),
               e);
        }
      }
      else
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_JSON_FORMATTED_REQUEST_DECODE_VALUE_CONTROL_NOT_OBJECT.get(
                  controlObject.toSingleLineString(),
                  JSON_FIELD_CONTROLS));
      }
    }


    if (strict)
    {
      final List<String> unrecognizedFields =
           JSONControlDecodeHelper.getControlObjectUnexpectedFields(
                valueObject, JSON_FIELD_CONTROLS);
      if (! unrecognizedFields.isEmpty())
      {
        throw new LDAPException(ResultCode.DECODING_ERROR,
             ERR_JSON_FORMATTED_REQUEST_DECODE_UNRECOGNIZED_FIELD.get(
                  controlObject.toSingleLineString(),
                  unrecognizedFields.get(0)));
      }
    }


    return new JSONFormattedRequestControl(jsonControl.getCriticality(),
         valueObject, Collections.unmodifiableList(controlObjectsList));
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void toString(@NotNull final StringBuilder buffer)
  {
    buffer.append("JSONFormattedRequestControl(isCritical=");
    buffer.append(isCritical());

    if (encodedValue != null)
    {
      buffer.append(", valueObject=");
      encodedValue.toSingleLineString(buffer);
    }

    buffer.append(')');
  }
}
