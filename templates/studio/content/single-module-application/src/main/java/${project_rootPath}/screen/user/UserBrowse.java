/*
 * Copyright 2020 Haulmont.
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
 */

package ${project_rootPackage}.screen.user;

import ${project_rootPackage}.entity.User;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;

@UiController("${project_idPrefix}_User.browse")
@UiDescriptor("user-browse.xml")
@LookupComponent("usersTable")
@LoadDataBeforeShow
@Route("users")
public class UserBrowse extends StandardLookup<User> {
}