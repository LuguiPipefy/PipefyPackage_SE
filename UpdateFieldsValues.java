package com.automationanywhere.botcommand.samples.commands.basic;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.i18n.Messages;
import com.automationanywhere.commandsdk.i18n.MessagesFactory;
import com.automationanywhere.core.security.SecureString;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Stack;

import static com.automationanywhere.commandsdk.model.AttributeType.*;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 * <pre>
 * updateFieldsValues allows for inserting values into Pipefy's Cards.
 *
 * </pre>
 *
 * @author Luis Guilherme de Azevedo e Silva
 */

//BotCommand makes a class eligible for being considered as an action.
@BotCommand

//CommandPks adds required information to be dispalable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "updateFieldsValues", label = "[[UpdateFieldsValues.label]]",
        node_label = "[[UpdateFieldsValues.node_label]]", description = "[[UpdateFieldsValues.description]]", icon = "pipefy.svg",

        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[UpdateFieldsValues.return_label]]", return_description = "[[UpdateFieldsValues.return_description]]", return_type = STRING, return_required = true)

public class UpdateFieldsValues {
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
            @Pkg(label = "[[UpdateFieldsValues.session.label]]", default_value_type = STRING, default_value = "Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String sessionName,


            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = CREDENTIAL)//TEXT)
            //UI labels.
            @Pkg(label = "[[UpdateFieldsValues.pipefyToken.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            //String pipefyToken,
            SecureString pipefyToken,

            @Idx(index = "3", type = TEXT)
            //UI labels.
            @Pkg(label = "[[UpdateFieldsValues.cardID.label]]", description = "[[UpdateFieldsValues.cardID.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String cardID,

            @Idx(index = "4", type = DICTIONARY)
            //UI labels.
            @Pkg(label = "[[UpdateFieldsValues.cardFields.label]]", description = "[[UpdateFieldsValues.cardFields.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            //Map<String, String>  insertDictionary) {
            Map<String, Value>  insertDictionary) {
        String line;
        //sobjects endpoint
        String result = "";
        StringBuffer responseContent = new StringBuffer();
        String message = "";
        String errorCode = "";

        //Create HashMap from session Map Object which was stored
        //Map<String, String> sessionValues = (Map<String, String>) sessionMap.get(sessionName);
        //if(sessionValues.get("sessionName") != sessionName)
        //  throw new BotCommandException(MESSAGES.getString("Session " + sessionName + " does not exist."));
        //Retrieve values from session Hashmap
        String loginURL = "https://api.pipefy.com/graphql";
        //String access_token =sessionValues.get("access_token");

        String requestBodyString = "";
        try {
            String urlWithParams = loginURL;

            if(pipefyToken != null){//.getInsecureString() != null){
                JSONObject insertJSON = new JSONObject();
                Stack<String> fieldsString = new Stack<String>();
                //for (Map.Entry<String,String> entry: insertDictionary.entrySet()) {
                for (Map.Entry<String, Value> entry: insertDictionary.entrySet()) {
                    insertJSON.put("field_id", entry.getKey());
                    insertJSON.put("field_value", "["+entry.getValue().toString()+"]");
                    fieldsString.push(insertJSON.toString());
                }
                // go from JSON to String to Encode
                requestBodyString = "mutation{updateFieldsValues(input:{\n" +
                        "                  nodeId: "+cardID+",\n" +
                        "                  values:"+fieldsString.toString().replace("]\"","\"]").replace("\"[","[\"").replace("\"field_value\"","value").replace("\"field_id\"","fieldId")+
                        " }){\n" +
                        "                   clientMutationId\n" +
                        "                   success\n" +
                        "                   userErrors{\n" +
                        "                  field\n" +
                        "                  message\n" +
                        "                  }\n" +
                        "                 }}";
                StringEntity entity = new StringEntity("query="+URLEncoder.encode(requestBodyString, StandardCharsets.UTF_8), ContentType.APPLICATION_FORM_URLENCODED);
                HttpClient client = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(urlWithParams);
                request.addHeader("Authorization", "Bearer " + pipefyToken.getInsecureString());
                //request.addHeader("Authorization", "Bearer " + pipefyToken);
                request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                request.setEntity(entity);

                //Execute HTTP Request
                HttpResponse response = client.execute(request);
                int actualResponseCode = response.getStatusLine().getStatusCode();

                //Response should be JSON, read and parse
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

                //Assuming JSON response
                JSONObject jsonResponse = new JSONObject(responseContent.toString());

                if (jsonResponse.get("data") != null) {
                    //Returning Object ID for success
                    result = jsonResponse.get("data").toString();
                } else {
                    //success wasnt returned, sending error message formatted cleanly for user
                    result = "ResponseCode: "+ Integer.toString(actualResponseCode)  +" Error Occured: "+jsonResponse.getString("errorCode")+", Error Message: "+ jsonResponse.getString(message);
                }

            } else {
                //Set error message for empty dictionary
                result = "Token is empty. Token is needed to create a card.";
            }
        } catch (Exception e) {
            //including full payload of error so user has full understanding of response from the API
            result = result + " Exception Occured: " + e.getMessage() + "|| Payload Sent:" + requestBodyString + " || Response Content: " + responseContent.toString() + " || Error Line: " + e.getStackTrace()[0].getLineNumber() ;
        } finally {
            //Return StringValue.
            return new StringValue(result.toString());
            //return new StringValue(result);
        }
    }

    // Ensure that a public setter exists.
    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}