package Routes;

import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Api(value = "/", description = "This is the default window")
public class App {

    /**
     *
     * @return Welcome information for users. Main view is presented.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        return "<h1> WELCOME TO A TEST OF HACKERNEWS</h1>";
    }


}
