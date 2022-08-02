package com.automationanywhere.botcommand.samples.commands.basic;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import static com.automationanywhere.commandsdk.model.AttributeType.DICTIONARY;
import static com.automationanywhere.commandsdk.model.AttributeType.TEXT;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 *<pre>
 UpdateObject allows for updating salesforce object properties. No response is given from the REST patch endpoint, so Object Updated Successfully is used when update is successful.

 * </pre>
 *
 * @author Micah Smith
 */

//BotCommand makes a class eligible for being considered as an action.
@BotCommand

//CommandPks adds required information to be dispalable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "updateobject", label = "[[UpdateObject.label]]",
        node_label = "[[UpdateObject.node_label]]", description = "[[UpdateObject.description]]", icon = "pipefy.svg",

        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[UpdateObject.return_label]]", return_type = STRING, return_required = true)


public class UpdateObject {
    //Messages read from full qualified property file name and provide i18n capability.
    private static final Messages MESSAGES = MessagesFactory
            .getMessages("com.automationanywhere.botcommand.samples.messages");

    @Sessions
    private Map<String, Object> sessionMap;

    //Identify the entry point for the action. Returns a Value<String> because the return type is String.
    @Execute
    public StringValue action(
            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "1", type = TEXT)
            //UI labels.
            @Pkg(label = "[[Authenticate.session.label]]", default_value_type = STRING, default_value="Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String sessionName,

            //Idx 2 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = TEXT)
            //UI labels.
            @Pkg(label = "[[UpdateObject.objectType.label]]",description = "[[UpdateObject.objectType.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String objectType,

            //Idx 3 would be displayed first, with a text box for entering the value.
            @Idx(index = "3", type = TEXT)
            //UI labels.
            @Pkg(label = "[[UpdateObject.objectID.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String objectID,

            //Idx 4 would be displayed first, with a text box for entering the value.
            @Idx(index = "4", type = DICTIONARY)
            //UI labels.
            @Pkg(label = "[[UpdateObject.fieldToUpdate.label]]", description = "[[UpdateObject.fieldToUpdate.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    Map<String, Value>  insertDictionary){

        String line;
        //sobjects endpoint
        String grantService = "/services/data/v49.0/sobjects/";
        String result = "";
        StringBuffer responseContent = new StringBuffer();
        String message ="";
        String errorCode = "";

        //Create HashMap from session Map Object which was stored
        Map<String, String> sessionValues = (Map<String, String>) sessionMap.get(sessionName);
        if(sessionValues.get("sessionName") != sessionName)
            throw new BotCommandException(MESSAGES.getString("Session " + sessionName + " does not exist."));
        //Retrieve values from session Hashmap
        String loginURL = sessionValues.get("baseLoginURL");
        String access_token =sessionValues.get("access_token");

        try {
            String urlWithParams = loginURL +
                    grantService + objectType + "/" + objectID;

            if(insertDictionary.size() > 0){
                //Building JSON with input values to later be converted to StringEntity for URL Encoding
                org.json.JSONObject insertJSON = new org.json.JSONObject();
                for (Map.Entry<String,Value> entry: insertDictionary.entrySet())
                    insertJSON.put(entry.getKey(),entry.getValue());
                // go from JSON to String to Encode
                String requestBodyString = insertJSON.toString();
                StringEntity entity = new StringEntity(requestBodyString, ContentType.APPLICATION_FORM_URLENCODED);

                //Note: using apache HTTP client as the salesforce API requires a patch request for updates
                HttpClient client = HttpClientBuilder.create().build();
                HttpPatch request = new HttpPatch(urlWithParams);
                request.setHeader("Content-type", "application/json");
                request.setHeader("Authorization", "Bearer " + access_token);
                request.setEntity(entity);
                //Execute HTTP Request
                HttpResponse response = client.execute(request);
                int actualResponseCode = response.getStatusLine().getStatusCode();

                if (actualResponseCode == 204){
                    //No JSON response for success, just setting a dummy value to check for
                    result = "Object Updated Successfully";
                }else{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }

                    //Error message returns array - loop through array to get error message out.
                    JSONArray jsonResponse = new JSONArray(responseContent.toString());
                    for (int i = 0 ; i < jsonResponse.length(); i++){
                        JSONObject obj = jsonResponse.getJSONObject(i);
                        if (obj.has("message")) {
                            message = obj.getString("message");
                        }

                        if (obj.has("errorCode")){
                            errorCode = obj.getString("errorCode");
                        }
                    }
                    result = "Error Occured. with code: " + errorCode + ". Message: " + message;
                }

            } else{
                result = "Dictionary is empty. Add at least one object property to update.";
            }

        } catch (Exception e) {
            result = result + " Exception Occured: " + e.getMessage();
        } finally {
            //Return StringValue.
            return new StringValue(result);
        }
    }
    // Ensure that a public setter exists.
    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}
