/*
* Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package ${package};

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Generated;
import javax.enterprise.context.Dependent;

<#list additionalImports as import>
import ${import};
</#list>

@Generated("${genClassName}")
@Dependent
public class ${className} implements ${interfaceName} {

    private static final Map<${keyType}, Predicate<${argumentType}>> PREDICATES_MAP;

    static {
        Map<${keyType}, Predicate<${argumentType}>> predicatesMap = new HashMap<>(${predicatesNum});

        <#list predicateDetails as predicate>
        predicatesMap.put(${predicate.key}, new ${predicate.predicateClass}());
        </#list>

        PREDICATES_MAP = Collections.unmodifiableMap(predicatesMap);
    }

    @Override
    public boolean test(Supplier<${keyType}> keySupplier, Supplier<${argumentType}> argumentSupplier) {
        return Optional.ofNullable(PREDICATES_MAP.get(keySupplier.get()))
                .orElse(t -> false)
                .test(argumentSupplier.get());
    }
}