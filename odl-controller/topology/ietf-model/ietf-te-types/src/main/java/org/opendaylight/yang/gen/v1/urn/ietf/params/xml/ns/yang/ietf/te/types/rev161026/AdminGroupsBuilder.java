package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev161026;


/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 *
 */
public class AdminGroupsBuilder {

    public static AdminGroups getDefaultInstance(java.lang.String defaultValue) {
        try {
            return new AdminGroups(AdminGroup.getDefaultInstance(defaultValue));
        } catch (final IllegalArgumentException e) {
            return new AdminGroups(ExtendedAdminGroup.getDefaultInstance(defaultValue));
        }
    }
}
