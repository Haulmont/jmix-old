/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl.jpql.tree;

import io.jmix.core.impl.jpql.ErrorRec;
import io.jmix.core.impl.jpql.QueryBuilder;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import java.util.List;

public class SelectedItemNode extends BaseCustomNode {
    private SelectedItemNode(Token token) {
        super(token);
    }

    public SelectedItemNode(int type) {
        this(new CommonToken(type, ""));
    }

    @Override
    public String toString() {
        return "SELECTED_ITEM";
    }

    @Override
    public Tree dupNode() {
        SelectedItemNode result = new SelectedItemNode(token);
        dupChildren(result);
        return result;
    }


    @Override
    public CommonTree treeToQueryPre(QueryBuilder sb, List<ErrorRec> invalidNodes) {
        CommonTree prevNode = getPrevNode();
        if (prevNode != null && prevNode instanceof SelectedItemNode) {
            sb.appendString(", ");
        } else {
            sb.appendSpace();
        }
        return super.treeToQueryPre(sb, invalidNodes);
    }
}