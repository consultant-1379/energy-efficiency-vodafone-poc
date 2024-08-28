package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev161026;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 *
 */
public class TeTpIdBuilder {

    public static TeTpId getDefaultInstance(java.lang.String defaultValue) {
        try {
            return new TeTpId(new IpAddress(defaultValue.toCharArray()));
        } catch (final IllegalArgumentException e) {
            return new TeTpId(java.lang.Long.valueOf(defaultValue));
        }
    }

}
