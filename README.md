**Use this lib can help make a multi-section list with RecyclerView**

1) SectionAdapter is RecyclerView.Adapter

2) SectionAdapter is base on GridLayoutManager

3) SectionAdapter if each row is only one grid it's like ListView

4) SectionAdapter can set echo section data a title view

5) SectionAdapter can hide title for a empty section

6) SectionAdapter can set how grid a row for a section.

**How to use it!**

In your `build.gradle`

```gradle
compile 'com.kylins.libs:sectionadapter:1.0.0'
```

1) Make a class extend SectionAdapter

2) Implementation onCreateSectionViewHolder and onBindViewHolder

3) RecyclerView.setAdapter(instance) and RecyclerView.setLayoutManager(instance.buildGridLayoutManager(Context))

