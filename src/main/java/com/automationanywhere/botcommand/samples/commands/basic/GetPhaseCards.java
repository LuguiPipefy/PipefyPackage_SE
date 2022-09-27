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
 * GetPhaseCards allows you to get cards from a specific Phase.
 *
 * </pre>
 *
 * @author Luis Guilherme de Azevedo e Silva / João Ferreira
 */

//BotCommand makes a class eligible for being considered as an action.
@BotCommand

//CommandPks adds required information to be dispalable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "GetPhaseCards", label = "[[GetPhaseCards.label]]",
        node_label = "[[GetPhaseCards.node_label]]", description = "[[GetPhaseCards.description]]", icon = "pipefy.svg",



        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[GetPhaseCards.return_label]]", return_description = "[[GetPhaseCards.return_description]]", return_type = STRING, return_required = true)

public class GetPhaseCards {
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
            @Pkg(label = "[[GetPhaseCards.session.label]]", default_value_type = STRING, default_value = "Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String sessionName,

            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = CREDENTIAL)
            //UI labels.
            @Pkg(label = "[[GetPhaseCards.pipefyToken.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            SecureString pipefyToken,

            @Idx(index = "3", type = TEXT)
            //UI labels.
            @Pkg(label = "[[GetPhaseCards.phaseId.label]]", description = "[[GetPhaseCards.phaseId.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String phaseID

    ) {
        String line;
        //sobjects endpoint
        String result = "";
        StringBuffer responseContent = new StringBuffer();
        String message = "";
        String errorCode = "";

        String loginURL = "https://api.pipefy.com/graphql";
        String requestBodyString = "";
        String queryGraphqlPipe="query{\n" +
                "          phase(id: "+ phaseID +") {\n" +
                "            id\n" +
                "            cards_count\n" +
                "            cards {\n" +
                "              edges{\n" +
                "                node{\n" +
                "                      id\n" +
                "                      age\n" +
                "                      assignees {\n" +
                "                        name\n" +
                "                        email\n" +
                "                        id\n" +
                "                        locale\n" +
                "                        timeZone\n" +
                "                      }\n" +
                "                      comments {\n" +
                "                        id\n" +
                "                        text\n" +
                "                        created_at\n" +
                "                        author_name\n" +
                "                      }\n" +
                "                      comments_count\n" +
                "                      createdAt\n" +
                "                      createdBy {\n" +
                "                        email\n" +
                "                        id\n" +
                "                        locale\n" +
                "                        name\n" +
                "\t\t\t\t\t\t            timeZone\n" +
                "                      }\n" +
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
                "                        phases{\n" +
                "                          id\n" +
                "                          name\n" +
                "                          fields{\n" +
                "                            id\n" +
                "                            label\n" +
                "                            description\n" +
                "                            type\n" +
                "                          }\n" +
                "                        }\n" +
                "                        start_form_fields{\n" +
                "                          id\n" +
                "                          label\n" +
                "                          description\n" +
                "                          type\n" +
                "                        }\n" +
                "                      }\n" +
                "                      started_current_phase_at\n" +
                "                      title\n" +
                "                      updated_at\n" +
                "                      url\n" +
                "                      fields {\n" +
                "                        field{\n" +
                "                          label\n" +
                "                          id\n" +
                "                          type\n" +
                "                        }\n" +
                "                        name\n" +
                "                        value\n" +
                "                        array_value\n" +
                "                        report_value\n" +
                "                        date_value\n" +
                "                        datetime_value\n" +
                "                        float_value\n" +
                "                        filled_at\n" +
                "                        updated_at\n" +
                "                        phase_field {\n" +
                "                          id\n" +
                "                          label\n" +
                "                          phase {\n" +
                "                            id\n" +
                "                            name\n" +
                "                          }\n" +
                "                        }\n" +
                "                        label_values {\n" +
                "                          id\n" +
                "                          name\n" +
                "                        }\n" +
                "                        assignee_values {\n" +
                "                          id\n" +
                "                          email\n" +
                "                        }\n" +
                "                      }\n" +
                "                      phases_history {\n" +
                "                        duration\n" +
                "                        phase {\n" +
                "                          id\n" +
                "                          name\n" +
                "                        }\n" +
                "                        firstTimeIn\n" +
                "                        lastTimeIn\n" +
                "                        lastTimeOut\n" +
                "                        created_at\n" +
                "                        became_late\n" +
                "                      }\n" +
                "                    }\n" +
                "                    }\n" +
                "                    }}\n" +
                "        }";
        try {
            String urlWithParams = loginURL;

            if(pipefyToken.getInsecureString() != null){
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
                    //Returning Object ID for success
                    result = jsonResponse.get("data").toString();
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
