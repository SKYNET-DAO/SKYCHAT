package cn.wildfire.imshat.kit.search;

import java.util.List;

import cn.wildfire.imshat.kit.search.module.ChannelSearchModule;
import cn.wildfire.imshat.kit.search.module.ContactSearchModule;
import cn.wildfire.imshat.kit.search.module.ConversationSearchModule;
import cn.wildfire.imshat.kit.search.module.GroupSearchViewModule;
import cn.wildfirechat.imshat.R;

public class SearchPortalActivity extends SearchActivity {
    @Override
    protected void initSearchModule(List<SearchableModule> modules) {

        SearchableModule module = new ContactSearchModule();
        modules.add(module);

        module = new GroupSearchViewModule();
        modules.add(module);

        module = new ConversationSearchModule();
        modules.add(module);
       // modules.add(new ChannelSearchModule());
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        setTitle(getString(R.string.str_search_center));
    }
}
