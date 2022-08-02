package com.automationanywhere.botcommand.samples.commands.basic;

import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import static com.automationanywhere.commandsdk.model.AttributeType.TEXT;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 * <pre>
 * deleteObject allows for deleteing salesforce objects. Success is returned on success as the response from the API is blank
 *
 * </pre>
 *
 * @author Micah Smith
 */

//BotCommand makes a class eligible for being considered as an action.
@BotCommand

//CommandPks adds required information to be dispalable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "deleteobject", label = "[[deleteObject.label]]",
        node_label = "[[deleteObject.node_label]]", description = "[[deleteObject.description]]", icon = "pipefy.svg",

        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[deleteObject.return_label]]", return_description = "[[deleteObject.return_description]]", return_type = STRING, return_required = true)

public class DeleteObject {
    //Messages read from full qualified property file name and provide i18n capability.
    private static final Messages MESSAGES = MessagesFactory
            .getMessages("com.automationanywhere.botcommand.samples.messages");

    @Sessions
    private Map<String, Object> sessionMap;

    //Identify the entry point for the action. Returns a Value<String> because the return type is String.
    @Execute
    public StringValue action(
            @Idx(index = "1", type = TEXT)
            //UI labels.
            @Pkg(label = "[[Authenticate.session.label]]", default_value_type = STRING, default_value = "Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String sessionName,

            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = TEXT)
            //UI labels.
            @Pkg(label = "[[delete.objectType.label]]", description = "[[delete.objectType.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String objectType,

            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "3", type = TEXT)
            //UI labels.
            @Pkg(label = "[[delete.objectID.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String objectID) {
        String line;
        //sobjects endpoint
        String grantService = "/services/data/v49.0/sobjects/";
        String result = "";
        StringBuffer responseContent = new StringBuffer();
        String message = "";
        String errorCode = "";

        //Create HashMap from session Map Object which was stored
        Map<String, String> sessionValues = (Map<String, String>) sessionMap.get(sessionName);
        if(sessionValues.get("sessionName") != sessionName)
            throw new BotCommandException(MESSAGES.getString("Session " + sessionName + " does not exist."));
        //Retrieve values from session Hashmap
        String loginURL = sessionValues.get("baseLoginURL");
        String access_token =sessionValues.get("access_token");

        //Build URL for REST call
        try {
            String urlWithParams = loginURL +
                    grantService + objectType +
                    "/" + objectID;

            HttpClient client = HttpClientBuilder.create().build();
            //REST Delete Request
            HttpDelete request = new HttpDelete(urlWithParams);
            request.setHeader("Authorization", "Bearer " + access_token);
            //Execute HTTP Request
            HttpResponse response = client.execute(request);
            int actualResponseCode = response.getStatusLine().getStatusCode();
            //Check for response
            if (actualResponseCode == 204) {
                //Setting result message for success
                result = "Successfully deleted " + objectID;
            } else {
                //Error message comes as JSON array - read and parse
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
                //Set error message in friendly format
                result = "Error Occured. with code: " + errorCode + ". Message: " + message;
            }

        } catch (Exception e) {
            //Setting full error message if try block failed to whatever result was currently set to + the exception message
            result = result + " Exception Occured: " + e.getMessage();
        } finally {
            //Return StringValue - could be success or failure depending on execution
            return new StringValue(result);
        }
    }

    // Ensure that a public setter exists.
    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}
