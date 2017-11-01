package settings;

import Routes.App;
import Routes.ItemRoute;
import Routes.TestRoute;
import Routes.UserRoute;
import io.swagger.jaxrs.config.BeanConfig;
import javax.ws.rs.ApplicationPath;
import java.util.HashSet;
import java.util.Set;

//Defines the base URI for all resource URIs.
@ApplicationPath("/")
//The java class declares root resource and provider classes
public class Application extends javax.ws.rs.core.Application {

    public Application() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setContact("Alexander Falk - alexfalk7@gmail.com");
        beanConfig.setTitle("Hackernews API");
        beanConfig.setResourcePackage("Routes");
        beanConfig.setBasePath("/api");
    }

    //The method returns a non-empty collection with classes, that must be included in the published JAX-RS application
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add( App.class );
        classes.add( UserRoute.class );
        classes.add( ItemRoute.class );
        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        classes.add(TestRoute.class );
        return classes;
    }

}
