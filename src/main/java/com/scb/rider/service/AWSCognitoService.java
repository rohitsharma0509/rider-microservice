package com.scb.rider.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.scb.rider.config.security.AWSCognitoCredentialsProvider;
import com.scb.rider.exception.AccessDeniedException;
import com.scb.rider.exception.UpdatePhoneNumberException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AWSCognitoService {

    private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;

    @SneakyThrows
    @Autowired
    public AWSCognitoService(AWSCognitoCredentialsProvider awsCognitoCredentialsProvider) {
        this.awsCognitoIdentityProvider = awsCognitoCredentialsProvider.awsCredentialsProvider();
    }



    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    public AdminDeleteUserResult deleteUserByPhoneNumber(String phoneNumber) {
        try{
            ListUsersRequest listUsersRequest = new ListUsersRequest();
            listUsersRequest.withUserPoolId(userPoolId);
            listUsersRequest.withFilter("phone_number=\"" + phoneNumber + "\"");
            ListUsersResult list = awsCognitoIdentityProvider.listUsers(listUsersRequest);
            if(list.getUsers().size() == 0){
                log.info("Number does not exists in cognito");
                return null;
            }
            log.info("Deleting user with userId : {}", list.getUsers().get(0).getUsername());
            String userName = list.getUsers().get(0).getUsername();
            AdminDeleteUserRequest request = new AdminDeleteUserRequest();
            request.withUserPoolId(userPoolId);
            request.withUsername(userName);
            return awsCognitoIdentityProvider.adminDeleteUser(request);
        }
        catch (Exception e){
            log.error("Exception occured in deleting the user {}",e.getMessage());
            throw new UpdatePhoneNumberException("COGNITO_EXCEPTION");
        }

    }

}
