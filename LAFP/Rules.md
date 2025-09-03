### Identifying row selection
- `get-item` followed by `comparision` statement

### Comparison statement
```
comp_statement: comp+;
comp: rel_operator rel_operand rel_operand;
rel_operator: gt | ge | lt | le | ne | eq | binary_op | unary_op;
rel_operand: literal | comp | dataframe | series;
```
Either both operand must end at same node or one/both of them should be literal. Then we can move this whole subtree untill a point where any column used here is not affected



## Rules
- `sx` = Single or sequence of operations.
- Flow graph will not have conditional/looping statements
- `Row selection statement`: get-item followed by conditional (<,>,<=,>=,!=) or connector (and, or) statement
- `sx,sy` means sx and sy are sequential in flow graph

|S.No| Rule | Description/Example/PreCondition   |
|:---:|------|---------------|
|  1 | s1, s2 => s2, s2   | [`genset`(s1) &cup; `killset`(s1)]  &cap; [`genset`(s2) &cup; `killset`(s2)] == &varnothing;|
| 2| s1, s2 => s3|[`genset`(s1) &cup; `killset`(s1)]  &cap; [`genset`(s2) &cup; `killset`(s2)] != &varnothing;|
| 3 | s1, s2 => s3 | s1, s2, s3 are row selection operation on same dataframe. <br/> If `s1` = df[condition1x...], `s2` = df[condition2x...] <br/>then `s3` = df[(condition1x..) **AND** (condition2x...)] |
 



<!-- Style -->
<style>
table {
    border-collapse: collapse;
}
table, th, td {
   border: 1px solid black;
}
blockquote {
    border-left: solid blue;
	padding-left: 10px;
}
</style>

