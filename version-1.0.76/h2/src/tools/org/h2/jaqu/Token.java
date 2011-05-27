/*
 * Copyright 2004-2008 H2 Group. Multiple-Licensed under the H2 License, 
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;

/**
 * Classes implementing this interface can be used as a token in a statement.
 */
interface Token {
//## Java 1.5 begin ##
    String getString(Query query);
//## Java 1.5 end ##
}
