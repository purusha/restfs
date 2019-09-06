package it.at.restfs.auth;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface Authorized {}
