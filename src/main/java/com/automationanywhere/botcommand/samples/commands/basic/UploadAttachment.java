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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Stack;

import static com.automationanywhere.commandsdk.model.AttributeType.*;
import static com.automationanywhere.commandsdk.model.DataType.STRING;

/**
 * <pre>
 * uploadAttachment allows for inserting salesforce objects. ObjectID is returned on successful response as is a success boolean value
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
        name = "uploadAttachment", label = "[[UploadAttachment.label]]",
        node_label = "[[UploadAttachment.node_label]]", description = "[[UploadAttachment.description]]", icon = "pipefy.svg",

        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_label = "[[UploadAttachment.return_label]]", return_description = "[[UploadAttachment.return_description]]", return_type = STRING, return_required = true)

public class UploadAttachment {
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
            @Pkg(label = "[[UploadAttachment.session.label]]", default_value_type = STRING, default_value = "Default")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String sessionName,

            //Idx 1 would be displayed first, with a text box for entering the value.
            @Idx(index = "2", type = CREDENTIAL)//TEXT)
            //UI labels.
            @Pkg(label = "[[UploadAttachment.pipefyToken.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            //String pipefyToken,
            SecureString pipefyToken,

            @Idx(index = "3", type = TEXT)
            //UI labels.
            @Pkg(label = "[[UploadAttachment.cardID.label]]", description = "[[UploadAttachment.cardID.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String cardId,

            @Idx(index = "4", type = TEXT)
            //UI labels.
            @Pkg(label = "[[UploadAttachment.fileName.label]]", description = "[[UploadAttachment.fileName.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String  fileName,

            @Idx(index = "5", type = FILE)
            //UI labels.
            @Pkg(label = "[[UploadAttachment.fileContent.label]]", description = "[[UploadAttachment.fileContent.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String  fileContent,

            @Idx(index = "6", type = TEXT)
            //UI labels.
            @Pkg(label = "[[UploadAttachment.fieldID.label]]", description = "[[UploadAttachment.fieldID.description]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty
            String  fieldId){
        String line;
        String result = "";
        StringBuffer responseContent = new StringBuffer();
        StringBuffer responseContentFile = new StringBuffer();
        StringBuffer responseContentAttachFile = new StringBuffer();
        String signedURL="";
        String signedFileName="";
        String message = "";
        String errorCode = "";
        String firstURL = "https://app.pipefy.com/upload_attachments/new";
        //Create HashMap from session Map Object which was stored
        //Map<String, String> sessionValues = (Map<String, String>) sessionMap.get(sessionName);
        //if(sessionValues.get("sessionName") != sessionName)
        //  throw new BotCommandException(MESSAGES.getString("Session " + sessionName + " does not exist."));
        //Retrieve values from session Hashmap
        //String access_token =sessionValues.get("access_token");

        String requestBodyString = "";
        try {
            String urlWithParams = firstURL+"?objectName="+URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            if(pipefyToken != null){//.getInsecureString() != null){
               //FIRST REQUEST:::: GET SignedURL    ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
                requestBodyString = "";
                HttpClient client = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet(urlWithParams);
                request.addHeader("Authorization", "Bearer " + pipefyToken.getInsecureString());
                request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                request.addHeader("Accept", "*/*");
                request.addHeader("Accept-Encoding", "gzip, deflate, br");
                request.addHeader("Connection", "keep-alive");

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

                if (jsonResponse.get("signedUrl") != null) {
                    signedURL = jsonResponse.get("signedUrl").toString();
                    signedFileName = jsonResponse.get("filename").toString();
                    //Returning Object ID for success
                } else {
                    //success wasnt returned, sending error message formatted cleanly for user
                    result = "ResponseCode: "+ Integer.toString(actualResponseCode)  +" Error Occured: "+jsonResponse.getString("errorCode")+", Error Message: "+ jsonResponse.getString(message);
                }

                //SECOND REQUEST:::: POST SignedURL + FileContent   ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
                File fileContentUpload = new File(fileContent);
                FileEntity fileEntity = new FileEntity(fileContentUpload, Files.probeContentType(Path.of(fileContent)));
                HttpClient clientFile = HttpClientBuilder.create().build();
                HttpPut requestFile = new HttpPut(signedURL);
                requestFile.setEntity(fileEntity);

                //Execute HTTP Request
                HttpResponse responseFile = clientFile.execute(requestFile);

                //Response should be JSON, read and parse
                BufferedReader readerFile = new BufferedReader(new InputStreamReader(responseFile.getEntity().getContent()));

                while ((line = readerFile.readLine()) != null) {
                    responseContentFile.append(line);
                }

                //THIRD REQUEST:::: POST AttachFile to Field   ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
                urlWithParams = "https://api.pipefy.com/graphql";
                // go from JSON to String to Encode
                requestBodyString = "mutation{updateFieldsValues(input:{\n" +
                        "                  nodeId: "+cardId+",\n" +
                        "                  values: [{fieldId:\""+fieldId+"\", value: \""+signedFileName+"\", operation: ADD}]\n" +
                        "                 }){\n" +
                        "                   clientMutationId\n" +
                        "                   success\n" +
                        "                    userErrors{\n" +
                        "                  field\n" +
                        "                  message\n" +
                        "                  }\n" +
                        "                 }}";
                StringEntity entity = new StringEntity("query="+URLEncoder.encode(requestBodyString, StandardCharsets.UTF_8), ContentType.APPLICATION_FORM_URLENCODED);
                HttpClient clientAttach = HttpClientBuilder.create().build();
                HttpPost requestAttach = new HttpPost(urlWithParams);
                requestAttach.addHeader("Authorization", "Bearer " + pipefyToken.getInsecureString());
                //requestAttach.addHeader("Authorization", "Bearer " + pipefyToken);
                requestAttach.addHeader("Content-Type", "application/x-www-form-urlencoded");
                requestAttach.setEntity(entity);

                //Execute HTTP Request
                HttpResponse responseAttach = clientAttach.execute(requestAttach);
                int actualResponseCodeAttach = responseAttach.getStatusLine().getStatusCode();

                //Response should be JSON, read and parse
                BufferedReader readerAttach = new BufferedReader(new InputStreamReader(responseAttach.getEntity().getContent()));

                while ((line = readerAttach.readLine()) != null) {
                    responseContentAttachFile.append(line);
                }

                //Assuming JSON response
                JSONObject jsonResponseAttach = new JSONObject(responseContentAttachFile.toString());

                if (jsonResponseAttach.get("data") != null) {
                    result = jsonResponseAttach.get("data").toString();
                } else {
                    //success wasnt returned, sending error message formatted cleanly for user
                    result = "ResponseCode: "+ Integer.toString(actualResponseCodeAttach)  +" Error Occured: "+jsonResponseAttach.getString("errorCode")+", Error Message: "+ jsonResponseAttach.getString(message);
                }
            } else {
                //Set error message for empty dictionary
                result = "Token is empty. Token is needed to use the Pipefy API.";
            }
        } catch (Exception e) {
            //including full payload of error so user has full understanding of response from the API
            result = result + " Exception Occured: " + e.getMessage() + "|| Payload Sent:" + requestBodyString + " || Response Content: " + responseContentAttachFile.toString() + " || Error Line: " + e.getStackTrace()[0].getLineNumber() ;
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