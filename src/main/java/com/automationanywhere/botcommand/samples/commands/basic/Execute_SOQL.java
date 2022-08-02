package com.automationanywhere.botcommand.samples.commands.basic;

import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.automationanywhere.commandsdk.model.AttributeType.TEXT;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 *<pre>
 Execute_SOQL executes a SOQL query via the Salesforce API to return matching objects as JSON

 * </pre>
 *
 * @author Micah Smith
 */

//BotCommand makes a class eligible for being considered as an action.
@BotCommand

//CommandPks adds required information to be dispalable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "execute_soql", label = "[[Execute_SOQL.label]]",
        node_label = "[[Execute_SOQL.node_label]]", description = "[[Execute_SOQL.description]]", icon = "pipefy.svg",

        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[Execute_SOQL.return_label]]", return_type = STRING, return_required = true)

public class Execute_SOQL {
    private static HttpURLConnection connection;
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
            @Pkg(label = "[[Authenticate.session.label]]", default_value_type = STRING, default_value="Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String sessionName,

            //Idx 2 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = TEXT)
            //UI labels.
            @Pkg(label = "[[Execute_SOQL.SOQL_Query.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
                    String SOQL_Query) {

        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        String grantService = "/services/data/v49.0/query/?q=";
        String result = "";


        //Create HashMap from session Map Object which was stored
        Map<String, String> sessionValues = (Map<String, String>) sessionMap.get(sessionName);
        if(sessionValues.get("sessionName") != sessionName)
            throw new BotCommandException(MESSAGES.getString("Session " + sessionName + " does not exist."));
        //Retrieve values from session Hashmap
        String loginURL = sessionValues.get("baseLoginURL");
        String access_token =sessionValues.get("access_token");

        try {
            String urlWithParams = loginURL +
                    grantService + URLEncoder.encode(SOQL_Query, StandardCharsets.UTF_8);


            URL url = new URL(urlWithParams);
            connection = (HttpURLConnection) url.openConnection();
            //Request Setup
            connection.setRequestMethod("GET");
            //set 5 second timeout for read and connect
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Authorization","Bearer "+ access_token);


            int status = connection.getResponseCode();
            //Check for success response from API call
            if (status > 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            //while loop going through each line of the failed response to build the response message
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }

            //reading response is complete, closer the reader
            reader.close();

            try {
                JSONObject response = new JSONObject(responseContent.toString());
                result = responseContent.toString();
            } catch (Exception e) {
                //On error, Salesforce returns a message not in JSON format, so this is used to return that to the user
                result = "Error: Response from Salesforce doesn't appear to be a valid JSON: " + responseContent.toString();
            }

        } catch (Exception e) {
            //send user result + error message should something fail in the try block
            result = result + " Error/Exception Occured: " + e.getMessage();
        } finally {
            //disconnect connection
            connection.disconnect();
        }

        //Return StringValue.
        return new StringValue(result);
    }

    // Ensure that a public setter exists.
    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}
