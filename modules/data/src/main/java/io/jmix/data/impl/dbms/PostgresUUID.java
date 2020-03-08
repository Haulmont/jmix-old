/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.jmix.data.impl.dbms;

import java.sql.SQLException;
import java.util.UUID;

public class PostgresUUID extends org.postgresql.util.PGobject
{
    private static final long serialVersionUID = -8115115840321643248L;

    public PostgresUUID(UUID uuid) throws SQLException {
        super();
        this.setType("uuid");
        this.setValue(uuid.toString());
    }
}
