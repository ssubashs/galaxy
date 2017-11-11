package com.fun.ms.common.service;

import com.atlassian.fugue.Either;

import java.util.concurrent.Callable;

public class Util {

    public static <T> Either<Exception, T> exceptionSafe(Callable<T> f) {
        try {
            return Either.right(f.call());
        }  catch (Exception e) {
            return Either.left(e);
        }
    }
}
