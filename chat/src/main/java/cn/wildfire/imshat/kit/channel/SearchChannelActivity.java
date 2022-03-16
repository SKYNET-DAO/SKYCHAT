package cn.wildfire.imshat.kit.channel;

import java.util.List;

import cn.wildfire.imshat.kit.search.SearchActivity;
import cn.wildfire.imshat.kit.search.SearchableModule;
import cn.wildfire.imshat.kit.search.module.ChannelSearchModule;

public class SearchChannelActivity extends SearchActivity {
    @Override
    protected void initSearchModule(List<SearchableModule> modules) {
        modules.add(new ChannelSearchModule());
    }
}
