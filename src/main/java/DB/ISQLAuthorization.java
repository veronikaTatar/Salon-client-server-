package DB;

import Salon.Authorization;
import Salon.Role;

public interface ISQLAuthorization {
    Role getRole(Authorization obj);
}
