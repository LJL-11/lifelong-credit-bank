package org.csu.creditbank.controller.admin;

import org.csu.creditbank.entity.Learner;
import org.csu.creditbank.service.LearnerService;
import org.csu.creditbank.util.InstitutionContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AdminLearnerNames {

    private AdminLearnerNames() {
    }

    public static <T> void fill(List<T> rows,
                                Function<T, Long> learnerIdGetter,
                                BiConsumer<T, String> learnerNameSetter,
                                LearnerService learnerService) {
        if (rows == null || rows.isEmpty()) return;
        List<Long> learnerIds = rows.stream()
                .map(learnerIdGetter)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (learnerIds.isEmpty()) return;

        Long tenantId = InstitutionContext.get();
        Map<Long, String> names;
        try {
            InstitutionContext.clear();
            names = learnerService.listByIds(learnerIds)
                    .stream()
                    .collect(Collectors.toMap(Learner::getId, Learner::getRealName, (a, b) -> a));
        } finally {
            if (tenantId != null) InstitutionContext.set(tenantId);
        }
        rows.forEach(row -> learnerNameSetter.accept(row, names.get(learnerIdGetter.apply(row))));
    }
}
