package ru.brandanalyst.frontend.yalets;

import net.sf.xfresh.core.InternalRequest;
import net.sf.xfresh.core.InternalResponse;
import net.sf.xfresh.core.xml.Xmler;
import net.sf.xfresh.db.AbstractDbYalet;
import ru.brandanalyst.frontend.services.LeftMenuManager;
import ru.brandanalyst.frontend.models.SimplyBrandForWeb;

import java.util.List;

public class ShowLeftMenuYalet extends AbstractDbYalet {

    public void process(InternalRequest req, InternalResponse res) {

        LeftMenuManager manager = new LeftMenuManager(jdbcTemplate);

        List<SimplyBrandForWeb> brandList = manager.getSearchResultByBrand();
        if (brandList != null) {
            res.add(brandList);
        } else {
            Xmler.Tag ans = Xmler.tag("error", "Трололо");
            res.add(ans);
        }
    }
}
