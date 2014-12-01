import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.filters.headers.SecurityHeadersFilter;

public class Global extends GlobalSettings {
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{SecurityHeadersFilter.class, GzipFilter.class};
    }
}