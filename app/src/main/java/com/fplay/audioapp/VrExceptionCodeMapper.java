package com.fplay.audioapp;

import com.joanzapata.android.asyncservice.api.ErrorMapper;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by User on 07-11-2016.
 */
public class VrExceptionCodeMapper implements ErrorMapper{
    public static final int EXCEPTION_RESTCLIENT_EXCEPTION = 101;
    public static final int EXCEPTION_NULL_POINTER = 102;
    public static final int EXCEPTION_GENERIC = 999;

    @Override
    public int mapError(Throwable throwable) {

        if (throwable instanceof HttpStatusCodeException)
            return ((HttpStatusCodeException) throwable).getStatusCode().value();
        else if ((throwable instanceof RestClientException))
        {
            return EXCEPTION_RESTCLIENT_EXCEPTION;
        }
        else if (throwable instanceof ResourceAccessException){

            return EXCEPTION_RESTCLIENT_EXCEPTION;
        }
        else if(throwable instanceof UnknownHostException){
            return EXCEPTION_RESTCLIENT_EXCEPTION;
        }
        else if(throwable instanceof UnknownHostException){
            return EXCEPTION_RESTCLIENT_EXCEPTION;
        }
        else if(throwable instanceof SocketTimeoutException){
            return EXCEPTION_RESTCLIENT_EXCEPTION;
        } else if(throwable instanceof InterruptedIOException){
            return EXCEPTION_RESTCLIENT_EXCEPTION;
        }
        else return EXCEPTION_GENERIC;
    }
}
