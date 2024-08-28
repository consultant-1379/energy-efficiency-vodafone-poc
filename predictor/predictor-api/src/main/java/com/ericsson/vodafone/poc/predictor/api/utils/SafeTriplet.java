/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.vodafone.poc.predictor.api.utils;

public class SafeTriplet<F, S, T, U, V> {

    public final F first;
    public final S second;
    public final T third;
    public final U fourth;
    public final V fifth;

    public SafeTriplet(final F first, final S second, final T third, final U fourth, final V fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof SafeTriplet)) {
            return false;
        }
        final SafeTriplet<?, ?, ?, ?, ?> p = (SafeTriplet<?, ?, ?, ?, ?>) o;
        return first.equals(p.first) && second.equals(p.second) && third.equals(p.third) && fourth.equals(p.fourth) && fifth.equals(p.fifth);
    }

    private static boolean equals(final Object x, final Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode())
                ^ (fourth == null ? 0 : fourth.hashCode()) ^ (fifth == null ? 0 : fifth.hashCode());
    }

    public static <F, S, T, U, V> SafeTriplet<F, S, T, U, V> create(final F f, final S s, final T t, final U u, final V v) {
        return new SafeTriplet<F, S, T, U, V>(f, s, t, u, v);
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        // TODO Auto-generated method stub

    }

}
