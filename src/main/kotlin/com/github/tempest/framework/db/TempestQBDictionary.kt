package com.github.tempest.framework.db

object TempestQBDictionary {
    val HAS_WHERE_QB_METHODS_CLASS = "\\Tempest\\Database\\Builder\\QueryBuilders\\HasWhereQueryBuilderMethods"
    val HAS_WHERE_QB_METHODS = listOf(
        "where",
        "whereField",
        "andWhere",
        "orWhere",
        "whereRaw",
        "andWhereRaw",
        "orWhereRaw",
    )
    val HAS_CONVENIENT_WHERE_METHODS_CLASS = "\\Tempest\\Database\\Builder\\QueryBuilders\\HasConvenientWhereMethods"
    val HAS_CONVENIENT_WHERE_METHODS = listOf(
        "whereIn",
        "whereNotIn",
        "whereBetween",
        "whereNotBetween",
        "whereNull",
        "whereNotNull",
        "whereNot",
        "whereLike",
        "whereNotLike",
        "orWhereIn",
        "orWhereNotIn",
        "orWhereBetween",
        "orWhereNotBetween",
        "orWhereNull",
        "orWhereNotNull",
        "orWhereNot",
        "orWhereLike",
        "orWhereNotLike",
        "whereToday",
        "whereYesterday",
        "whereThisWeek",
        "whereLastWeek",
        "whereThisMonth",
        "whereLastMonth",
        "whereThisYear",
        "whereLastYear",
        "whereAfter",
        "whereBefore",
        "orWhereToday",
        "orWhereYesterday",
        "orWhereThisWeek",
        "orWhereThisMonth",
        "orWhereThisYear",
        "orWhereAfter",
        "orWhereBefore",
        "whereField",
        "orWhere",
    )

    val SELECT_METHODS = listOf(
        "first",
        "all",
        "orderBy",
        "orderByRaw",
        "groupBy",
        "having",
        "join",
        "with",
        "raw",
        "bind",
        "build",
    )

    val WHERE_METHODS = buildList {
        addAll(HAS_WHERE_QB_METHODS)
        addAll(HAS_CONVENIENT_WHERE_METHODS)
    }
}
