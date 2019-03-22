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

package io.jmix.core.impl.jpql;

import io.jmix.core.impl.jpql.tree.BaseJoinNode;
import io.jmix.core.impl.jpql.tree.IdentificationVariableNode;
import io.jmix.core.impl.jpql.tree.QueryNode;
import io.jmix.core.impl.jpql.tree.SimpleConditionNode;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeVisitorAction;

import java.util.*;

public class IdVarSelector implements TreeVisitorAction {
    private QueryVariableContext root;

    private List<ErrorRec> invalidIdVarNodes = new ArrayList<>();
    private DomainModel model;
    private Deque<QueryVariableContext> stack = new ArrayDeque<>();

    public IdVarSelector(DomainModel model) {
        this.model = model;
    }

    public QueryVariableContext getContextTree() {
        return root;
    }

    @Override
    public Object pre(Object t) {
        if (!(t instanceof CommonTree))
            return t;

        CommonTree node = (CommonTree) t;

        if (node instanceof QueryNode) {
            QueryVariableContext newCurrent = new QueryVariableContext(model, (QueryNode) node);
            if (root == null) {
                root = newCurrent;
            }
            QueryVariableContext last = stack.peekLast();
            if (last != null) {
                last.addChild(newCurrent);
            }
            stack.addLast(newCurrent);
        }
        return t;
    }

    @Override
    public Object post(Object t) {
        if (!(t instanceof CommonTree))
            return t;

        if (t instanceof CommonErrorNode) {
            return t;
        }

        CommonTree node = (CommonTree) t;

        if (node.token == null)
            return t;


        if ((node instanceof QueryNode) && isInWhereSubquery(node)) {
            stack.peekLast().setPropagateVariablesUp(false);
            return t;
        }

        if (node instanceof IdentificationVariableNode) {
            IdentificationVariableNode vnode = (IdentificationVariableNode) node;
            vnode.identifyVariableEntity(model, stack, invalidIdVarNodes);
            return t;
        }

        if (node instanceof BaseJoinNode) {
            BaseJoinNode vnode = (BaseJoinNode) node;
            vnode.identifyVariableEntity(model, stack, invalidIdVarNodes);
            return t;
        }

        return t;
    }

    private boolean isInWhereSubquery(CommonTree node) {
        return node.getParent() != null && node.getParent() instanceof SimpleConditionNode;
    }

    public List<ErrorRec> getInvalidIdVarNodes() {
        return Collections.unmodifiableList(invalidIdVarNodes);
    }
}