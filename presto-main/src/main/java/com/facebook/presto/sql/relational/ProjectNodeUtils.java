/*
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
package com.facebook.presto.sql.relational;

import com.facebook.presto.spi.relation.RowExpression;
import com.facebook.presto.sql.planner.Symbol;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.SymbolReference;

import java.util.Map;
import java.util.stream.Collectors;

import static com.facebook.presto.sql.relational.OriginalExpressionUtils.castToExpression;
import static com.facebook.presto.sql.relational.OriginalExpressionUtils.castToRowExpression;

public class ProjectNodeUtils
{
    private ProjectNodeUtils() {}

    public static boolean isIdentity(ProjectNode projectNode)
    {
        for (Symbol symbol : projectNode.getAssignments().getSymbols()) {
            if (!isIdentity(projectNode, symbol)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIdentity(ProjectNode projectNode, Symbol output)
    {
        Expression expression = projectNode.getAssignments().get(output);

        return expression instanceof SymbolReference && ((SymbolReference) expression).getName().equals(output.getName());
    }

    public static Map<Symbol, Expression> getAsExpression(Map<Symbol, RowExpression> assignments)
    {
        return assignments.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> castToExpression(entry.getValue())));
    }

    public static Map<Symbol, RowExpression> getAsRowExpression(Map<Symbol, Expression> assignments)
    {
        return assignments.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> castToRowExpression(entry.getValue())));
    }
}
