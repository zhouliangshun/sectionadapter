package com.kylins.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zhouliangshun on 2016/11/29.
 */

public abstract class SectionAdapter<HW extends SectionAdapter.ViewHolder> extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {


    private ArrayMap<Integer, Integer> sectionsSize = new ArrayMap<>();
    private ArrayMap<Integer, View> sectionsView = new ArrayMap<>();
    private ArrayMap<Integer, Integer> sectionsPosition = new ArrayMap<>();
    private ArrayMap<Integer, Boolean> sectionsSkipEmpty = new ArrayMap<>();

    public static final int SECTION_MAX_COUNT = 10000;

    private int size = 0;
    private RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            size = calcItemSize();
            super.onChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            size = calcItemSize();
            super.onItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            size = calcItemSize();
            super.onItemRangeRemoved(positionStart, itemCount);
        }
    };

    /*private  GridLayoutManager.SpanSizeLookup sectionSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            IndexPath indexPath =  positionToIndexPath(position);
            if(indexPath.position==-1){
                return 1;
            }
            return getSectionSpanSize(indexPath.section,indexPath.position);
        }
    };

    public GridLayoutManager.SpanSizeLookup getSectionSpanSizeLookup() {
        return sectionSpanSizeLookup;
    }*/

    public GridLayoutManager buildGridLayoutManager(Context context){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,getSpanMaxSize(),GridLayoutManager.VERTICAL,false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                IndexPath indexPath =  positionToIndexPath(position);
                if(indexPath.position==-1){
                    return getSpanMaxSize();
                }
                return getSectionSpanSize(indexPath.section,indexPath.position);
            }
        });
        return gridLayoutManager;
    }

    /**
     * calc total size for all adapter
     * @return
     */
    private int calcItemSize() {

        sectionsSize.clear();
        sectionsView.clear();
        sectionsSkipEmpty.clear();
        sectionsPosition.clear();

        int size = 0;
        int sectionSize = getSectionCount();
        if(sectionSize>SECTION_MAX_COUNT){
            throw new RuntimeException("sections max size is "+SECTION_MAX_COUNT);
        }

        for (int i = 0; i < sectionSize; i++) {
            boolean isSkip = skipIfEmpty(i);
            sectionsSkipEmpty.put(i, isSkip);

            int secSize = getItemCount(i);
            sectionsSize.put(i, secSize);
            size += secSize;

            View secView = getSectionView(i);
            if(secView!=null)
                sectionsView.put(i, getSectionView(i));

            boolean skipEmpty = sectionsSkipEmpty.get(i);

            if (secSize == 0 && (skipEmpty||secView==null)){
                sectionsPosition.put(i,-1);
                continue;
            }

            sectionsPosition.put(i,size-secSize);

            if(secView!=null){
                size++;
            }
        }

        return size;
    }

    private IndexPath positionToIndexPath(int position){

        IndexPath index = new IndexPath();
        index.position = 0;
        index.section = 0;

        for(int i = 0;i<sectionsPosition.size();i++){
            int pos = sectionsPosition.get(i);
            if(pos == -1){
                continue;
            }
            int size = sectionsSize.get(i);
            int end = sectionsView.containsKey(i) ? pos+size+1 : pos+size;
            if(position < end){
                index.section = i;
                index.position = sectionsView.containsKey(i) ? position-pos-1 : position-pos;
                return index;
            }
        }

        return index;
    }

    @Override
    public final int getItemViewType(int position) {

        IndexPath indexPath =  positionToIndexPath(position);
        if(indexPath.position==-1){
            return indexPath.section;
        }

        int secItemViewType = getItemViewType(indexPath.section,indexPath.position);
        if(secItemViewType < 0){
            throw new RuntimeException("ItemViewType can't low zero");
        }

        return SECTION_MAX_COUNT + secItemViewType;
    }

    /**
     * get ItemView Item;
     * @param section section index
     * @param position index of section
     * @return can't not is zero,zero is section title
     */
    protected int getItemViewType(int section,int position){
        return 1;
    }

    /**
     * get section count
     *
     * @return section count,max size @see(SECTION_MAX_COUNT)
     */
    protected int getSectionCount(){
        return 0;
    }

    /**
     * get the
     * @param section
     * @param position
     * @return
     */
    protected int getSectionSpanSize(int section,int position){
        return 1;
    }

    protected int getSpanMaxSize(){
        return 1;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType<SECTION_MAX_COUNT){
            return new SectionTitleViewHolder(sectionsView.get(viewType));
        }
        return onCreateSectionViewHolder(parent,viewType-SECTION_MAX_COUNT);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IndexPath indexPath =  positionToIndexPath(position);
        if(indexPath.position!=-1){
            onBindViewHolder((HW) holder,indexPath.section,indexPath.position);
        }
    }

    protected boolean skipIfEmpty(int section) {
        return true;
    }

    protected View getSectionView(int section) {
        return null;
    }

    public abstract ViewHolder onCreateSectionViewHolder(ViewGroup parent, int viewType);
    public abstract void onBindViewHolder(HW holder, int section, int position);


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        registerAdapterDataObserver(adapterDataObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterAdapterDataObserver(adapterDataObserver);
        sectionsView.clear();
    }

    /**
     * size is sections size + section views
     *
     * @return
     */
    @Override
    public final int getItemCount() {
        return size;
    }


    public abstract int getItemCount(int section);


    public static abstract class  ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class SectionTitleViewHolder extends ViewHolder {
        public SectionTitleViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class IndexPath {
        int section;
        int position;
    }

}
