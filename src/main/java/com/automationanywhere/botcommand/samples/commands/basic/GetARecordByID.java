package com.automationanywhere.botcommand.samples.commands.basic;

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

import static com.automationanywhere.commandsdk.model.AttributeType.CREDENTIAL;
import static com.automationanywhere.commandsdk.model.AttributeType.TEXT;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 * <pre>
 * getARecordByID allows for inserting salesforce objects. ObjectID is returned on successful response as is a success boolean value
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
        name = "getARecordByID", label = "[[GetARecordByID.label]]",
        node_label = "[[GetARecordByID.node_label]]", description = "[[GetARecordByID.description]]", icon = "pipefy.svg",



        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[GetARecordByID.return_label]]", return_description = "[[GetARecordByID.return_description]]", return_type = STRING, return_required = true)

public class GetARecordByID {
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
            @Pkg(label = "[[GetARecordByID.session.label]]", default_value_type = STRING, default_value = "Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String sessionName,

            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = CREDENTIAL)
            //UI labels.
            @Pkg(label = "[[GetARecordByID.pipefyToken.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            SecureString pipefyToken,

            @Idx(index = "3", type = TEXT)
            //UI labels.
            @Pkg(label = "[[GetARecordByID.recordID.label]]", description = "[[GetARecordByID.recordID.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String recordID
    ) {
        String line;
        //sobjects endpoint
        String result = "";
        StringBuffer responseContent = new StringBuffer();
        String message = "";
        String errorCode = "";

        String loginURL = "https://api.pipefy.com/graphql";
        String requestBodyString = "";
        String queryGraphqlPipe="{\n" +
                "                    card (id: "+recordID+") {\n" +
                "                      id\n" +
                "                      uuid\n" +
                "                      age\n" +
                "                      createdBy{\n" +
                "                       id\n" +
                "                       username\n" +
                "                       email\n" +
                "                       phone\n" +
                "                       name\n" +
                "                       locale\n" +
                "                       displayName\n" +
                "                       timezone\n" +
                "                     }\n" +
                "                      attachments{\n" +
                "                          createdAt\n" +
                "                          createdBy {\n" +
                "                            id\n" +
                "                            name\n" +
                "                          }\n" +
                "                          field {\n" +
                "                            id\n" +
                "                            label\n" +
                "                          }\n" +
                "                          path\n" +
                "                          phase {\n" +
                "                            id\n" +
                "                            name\n" +
                "                          }\n" +
                "                          url\n" +
                "                        }\n" +
                "                      assignees {\n" +
                "                        name\n" +
                "                        email\n" +
                "                        phone\n" +
                "                        id\n" +
                "                        locale\n" +
                "                        timeZone\n" +
                "                      }\n" +
                "                       inbox_emails{\n" +
                "                       bcc\n" +
                "                       body\n" +
                "                       cc\n" +
                "                       from\n" +
                "                       fromName\n" +
                "                       id\n" +
                "                       main_to\n" +
                "                       message_id\n" +
                "                       sent_via_automation\n" +
                "                       state\n" +
                "                       subject\n" +
                "                       to\n" +
                "                       updated_at\n" +
                "                       user {\n" +
                "                         id\n" +
                "                       }\n" +
                "                       attachments {\n" +
                "                         id\n" +
                "                         fileUrl\n" +
                "                         filename\n" +
                "                         public_url\n" +
                "                       }\n" +
                "                       }\n" +
                "                        parent_relations{\n" +
                "                          name\n" +
                "                          pipe {\n" +
                "                            id\n" +
                "                          }\n" +
                "                          repo\n" +
                "                          cards{\n" +
                "                            id\n" +
                "                            createdAt\n" +
                "                            title\n" +
                "                          }\n" +
                "                        }\n" +
                "                        child_relations{\n" +
                "                          name\n" +
                "                          pipe {\n" +
                "                            id\n" +
                "                          }\n" +
                "                          repo\n" +
                "                          cards{\n" +
                "                            id\n" +
                "                            createdAt\n" +
                "                            title\n" +
                "                          }\n" +
                "                        }\n" +
                "                      comments {\n" +
                "                        id\n" +
                "                        text\n" +
                "                        created_at\n" +
                "                        author_name\n" +
                "                      }\n" +
                "                      comments_count\n" +
                "                      createdAt\n" +
                "                      creatorEmail\n" +
                "                      current_phase {\n" +
                "                        id\n" +
                "                        name\n" +
                "                        sequentialId\n" +
                "                        done\n" +
                "                        cards_count\n" +
                "                      }\n" +
                "                      current_phase_age\n" +
                "                      done\n" +
                "                      due_date\n" +
                "                      emailMessagingAddress\n" +
                "                      expired\n" +
                "                      late\n" +
                "                      pipe {\n" +
                "                        cards_count\n" +
                "                        emailAddress\n" +
                "                        id\n" +
                "                        name\n" +
                "\n" +
                "                          phases{\n" +
                "                            id\n" +
                "                            name\n" +
                "                            fields{\n" +
                "                              id\n" +
                "                              label\n" +
                "                              description\n" +
                "                              type\n" +
                "                            }\n" +
                "                          }\n" +
                "                          start_form_fields{\n" +
                "                            id\n" +
                "                            label\n" +
                "                            description\n" +
                "                            type\n" +
                "                          }\n" +
                "\n" +
                "                      }\n" +
                "                      started_current_phase_at\n" +
                "                      title\n" +
                "                      updated_at\n" +
                "                      url\n" +
                "                      fields {\n" +
                "                        field{id}\n" +
                "                        array_value\n" +
                "                        name\n" +
                "                        value\n" +
                "                        report_value\n" +
                "                        date_value\n" +
                "                        datetime_value\n" +
                "                        float_value\n" +
                "                        filled_at\n" +
                "                        updated_at\n" +
                "                        assignee_values {\n" +
                "                              id\n" +
                "                              email\n" +
                "                            }\n" +
                "\n" +
                "                      }\n" +
                "                      phases_history {\n" +
                "                        duration\n" +
                "                        phase {\n" +
                "                          id\n" +
                "                          name\n" +
                "                        }\n" +
                "\n" +
                "                        firstTimeIn\n" +
                "                        lastTimeIn\n" +
                "                        lastTimeOut\n" +
                "                        created_at\n" +
                "                        became_late\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }";
        try {
            String urlWithParams = loginURL;

            if(pipefyToken != null){//.getInsecureString() != null){
                requestBodyString = queryGraphqlPipe;
                StringEntity entity = new StringEntity("query="+URLEncoder.encode(requestBodyString, StandardCharsets.UTF_8), ContentType.APPLICATION_FORM_URLENCODED);
                HttpClient client = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(urlWithParams);
                request.addHeader("Authorization", "Bearer " + pipefyToken.getInsecureString());
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
                    JSONObject jsonResponseData = new JSONObject(jsonResponse.get("data").toString());
                    //Returning Object ID for success
                    result = jsonResponseData.get("card").toString();
                } else {
                    //success wasnt returned, sending error message formatted cleanly for user
                    result = "ResponseCode: "+ Integer.toString(actualResponseCode)  +" Error Occured: "+jsonResponse.getString("errorCode")+", Error Message: "+ jsonResponse.getString(message);
                }

            } else {
                //Set error message for empty dictionary
                result = "Token is empty. Token is needed to return the data.";
            }
        } catch (Exception e) {
            //including full payload of error so user has full understanding of response from the API
            result = result + " Exception Occured: " + e.getMessage() + "|| Payload Sent:" + requestBodyString + " || Response Content: " + responseContent.toString() ;
        } finally {
            //Return StringValue.
            //return new StringValue(result);
            return new StringValue(result);
        }
    }

    // Ensure that a public setter exists.
    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}
