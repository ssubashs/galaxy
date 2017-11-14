package com.fun.ms.common.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import io.vertx.core.eventbus.ReplyException;

import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

public class Util {

    public static <T> Either<Exception, T> exceptionSafe(Callable<T> f) {
        try {
            return Either.right(f.call());
        }  catch (Exception e) {
            return Either.left(e);
        }
    }

    public static Response convertExceptionToApiResponse(Throwable cause) {
        if (cause instanceof ReplyException) {
            ReplyException replyException = (ReplyException) cause;
            return Response.status(replyException.failureCode())
                    .entity(new ErrorResponse(replyException.failureCode(),replyException.getMessage())).build();
        }
        else {
            return Response.status(500).entity(new ErrorResponse(500,cause.getMessage())).build();
        }
    }

    public static Pair<Integer,String> convertExceptionCodeAndMessage(Throwable cause) {
        if (cause instanceof ReplyException) {
            ReplyException replyException = (ReplyException) cause;
            return Pair.pair(replyException.failureCode(),replyException.getMessage());
        }
        else {
            return Pair.pair(500,cause.getMessage());
        }
    }
}
