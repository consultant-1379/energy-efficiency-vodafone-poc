/*global define */
define([
    'mapping/Leaflet'
], function (L) {

    var DEFAULT_SIZE = 32768;

    var latToNormY = function (lat) {
        // transform [ -85, +85] degrees to [-0.5, 0.5] -- but not truncated to that range
        return Math.log(Math.tan(( 1 + lat / 90) * Math.PI / 4)) / 2 / Math.PI;
    };

    var normYToLat = function (y) {
        // transform [0.5, 0.5] to [ -85, +85] degrees.
        return Math.atan(Math.exp(y * 2 * Math.PI)) * 360 / Math.PI - 90;
    };

    /**
     * Map transformation class.
     * There are 3 coordinate systems to take into account / transform between: The (x,y) coordinates of the the 'client',
     * the (lat,lng) latitude and longitude pair, and the (x,y) coordinates that Leaflet uses at the current zoom level
     * (and the screen / browser window coordinates, whichTab are a panned version of the same.
     * This class deals with the transformation between 'client' (x,y) coordinates and (lat, lng) coordinates.
     * The transformation from (lat,lng) is the spherical Mercator projection: The longitude get transformed linearly to x:
     *    x = x0 + size * (lng/360)
     * [with 'lng' in degrees, so that the width of the whole earth is 'size' pixels] but the transformation from latitude to y is
     * nonlinear, in such a way that the projection is conformal: locally it should be angle preserving, or equivalently the scale
     * should be independent of the direction.
     * On an infinitesimal scale 'dx' is proportional to 'dlng', but the actual distance depends on the latitude: a difference 'dlng' corresponds to
     * a distance of 'R * cos(lat) * dlng', with 'R' the radius of the earth. So a distance of 'dx' on the map corresponds to
     * 'R * cos(lat) * dx / size * 360' in real life.
     * In the y direction a difference 'dlat' corresponds to 'R * dlat', so a difference 'dy' corresponds to 'dy * (dlat/dy) * R'.
     * So to get the same scale x and y we need '360/size * cos(lat) = (dlat/dy)', so 'dy/dlat = size/360/cos(lat),'
     * and so y needs to equal 'factor * integral( 1/cos(lat) dlat)'.
     * The functions 'latToNormY' and 'normYToLat' do that conversion: 'latToNormY' converts lat to a normalized y, where [-85 deg, 85deg] maps
     * to [-0.5, 0.5], and 'normYToLat' does the inverse (+- 85 degrees is the limit to get a 'square' world map). Please
     * note that uses the mathematical convention of y increasing upwards.
     * With that, but using the computer graphics convention of y growing downwards the coordinate transform can be expressed as:
     *      x = x0 + size * (lng / 360)
     *      y = y0 - size * latToNormY(lat).
     * Internally, at zoom level Zoom Leaflet uses x, y coordinates with a size of 256 * 2^Zoom pixels, and (lat=0, lng=0) in the middle,
     * so that can be characterized by 'size = 256 * 2^Zoom', 'x0 = size/2' and 'y0 = size/2'.
     * The constructor allows creating a transform given (size, x0, y0), or from the size plus a corresponding pair
     * of (x,y) and (lat, lng) coordinates.
     * The size is the size in pixels of the whole earth, default 256 * 2^7, i.e. zoom level 7.
     * For the constructor, if 'x0' and 'y0' are specified then they will be used directly, otherwise if 'xy' and 'latLng'
     * are specified then the transform will be such that 'xy' maps to 'latLng' and vice versa.
     * If no coordinates are specified then 'x0' will be 0 and 'y0' will be 0.14 * 'size' so that (x,y) = (0, 0)
     * corresponds to (lag, lng) = (44.9, 0), east of Bordeaux.
     * It is also possible to create a transformation with the method fitWithRotation and [static?] fitWithoutRotation.
     * These take an array of objects with pos and latLng members, and do a least mean square fit to find the
     * transformation that best maps those, with or without a possible rotation.
     * Note: after 'transform2 = new Transform(transform1)' 'transform1' and 'transform1' are identical, unless
     * transform1 was created with fitWithRotation.
     *
     * @options
     * {Number} x0 - x coordinate for lng = 0
     * {Number} y0 - y coordinate for lat = 0
     * {L.Point} xy - (x,y) coordinates of the map "center"
     * {L.LatLng} latLng - (lat, lng) coordinates of the map center.
     * {Number} size - size in pixels of the whole earth.
     * @class widgets/NetworkMap/Transform
     * @constructor
     * @param {Object} options
     */

    /*
     We need to consider a number of cases:
     a) blank sheet: no points yet, no 'background'; say display at zoom 7. Choose a size of 2^7 * 256 (== Leaflet scale).
        center at the origin, but we want the top left of the window to be (x, y) = (0, 0), so the center of the map to be
        (x,y) = (w/2, h/2) with (w,h) the window size.
     b) We have points with (x,y), but no (lat, lng) yet: choose a transform to fit the map in the current window as above.
        Calculate the (x, y) bounding box and make the center of that correspond to (lat, lng) = (0, 0). As for the size:
        Calculate the width and height of the bounding box, increased by some padding, (dx, dy)  and the delta in normalized latitude and longitude:
        dxNorm = (lng_max - lng_min)/360 and dyNorm = yNorm(lat_max) - yNorm(lat_min).
        Then we want size * dxNorm >= dx and size * dyNorm >= dy, so set size = max(dx / dxNorm, dy / dyNorm).
     c) Place on world map. After the initial pan and zoom to the right area first do a zoom to fit again. Then on moving some points
        we want to shift, scale and rotate the untouched points, i.e. transforming their original (x, y) to new (lat, lng).
        => [ least squares,..].
        On commit we need to transform the resulting (lat, lng) back to (x,y), with a pan & zoom transform without rotation.
        The new (x,y) can be chosen rather arbitrarily, but we would like them to be positive but small. So choose such that the top left
        (lat, lng) point maps to (x,y) = 20, 20. As for the scale: we want the whole network to fit in ~ 1000 x 1000.
        So scale it such that x extends from 20 to 980 or y from 20 to 980, whichever extends further.
     d) We read a file that does have (lat,lng) as well as (x,y), at least for some points.
        Do a least squares fit for x = x0 + size * xnorm + x0, y = y0 - size * ynorm, and use that transform for any new points,
        or points without (lat, lng).
     ('zoom to fit' should keep the current transform, but pan & zoom the Leaflet map).
     */
    var Transform = function (options) {
        options = options || {};
        var size = this.size = options.size || DEFAULT_SIZE;
        if (options.x0 !== undefined && options.y0 !== undefined) {
            this.x0 = options.x0;
            this.y0 = options.y0;
        } else if (options.xy && options.latLng) {
            this.x0 = options.xy.x - size * options.latLng.lng / 360;
            this.y0 = options.xy.y + size * latToNormY(options.latLng.lat);
        } else {
            this.x0 = 0;
            this.y0 = 0.14 * size;
        }
        var x0 = this.x0;
        var y0 = this.y0;
        this.toXY = function (latLng) {
            return L.point(x0 + size * latLng.lng / 360, y0 - size * latToNormY(latLng.lat));
        };
        this.toLatLng = function (point) {
            return L.latLng(normYToLat((y0 - point.y) / size), 360 * (point.x - x0) / size);
        };
    };

    /**
     * Convert (lat,lng) to (x,y)
     * @method toXY
     * @param {L.LatLng} latLng
     * @return {L.point}
     */

    /**
     * Convert (x,y) to (lat,lng)
     * @method toLatLng
     * @param {L.point} xy
     * @return {L.LatLng}
     */

    /**
     * The x coordinate to whichTab lng = 0 is mapped.
     * @property {Number} x0
     */

    /**
     * The y coordinate to whichTab lat = 0 is mapped.
     * @property {Number} y0
     */

    /**
     * The size of the whole earth in pixels
     * @property {Number} size
     */

    /**
     * Convert a latitude to a normalized Y coordinate, according to the spherical Mercator projection.
     * +-85.05 degrees gets mapped to +-0.5.
     * @method latToNormY
     * @static
     * @param {Number} lat
     * @return {Number}
     */
    Transform.latToNormY = latToNormY;

    /**
     * Convert a normalized Y coordinate to a latitude, implementing the inverse of the spherical Mercator projection.
     * +-0.5. gets mapped to +-85.05 degrees.
     * @method normYToLat
     * @static
     * @param {Number} y
     * @return {Number}
     */

    Transform.normYToLat = normYToLat;

    var calculateSums = function (vertices) {
        // (X, Y) are normalized from (lat, lng), i.e. in [-0.5, 0.5]; // (x, y) are in pixels.
        var sums = {
            N: 0, X: 0, Y: 0, XX: 0, YY: 0, x: 0, y: 0,
            Xx: 0, Xy: 0, Yx: 0, Yy: 0
        };

        for (var i in vertices) {
            var p = vertices[i];
            if (!p.latLng || !p.pos) {
                continue;
            }
            var X = p.latLng.lng / 360;
            var Y = latToNormY(p.latLng.lat);
            sums.N += 1;
            sums.X += X;
            sums.Y += Y;
            sums.XX += X * X;
            sums.YY += Y * Y;
            sums.x += p.pos.x;
            sums.y += p.pos.y;
            sums.Xx += X * p.pos.x;
            sums.Xy += X * p.pos.y;
            sums.Yx += Y * p.pos.x;
            sums.Yy += Y * p.pos.y;
        }
        return sums;
    };

    /**
     * Calculate a new transformation that best matches for the given vertices. The resulting transformation is a translation / scaling / rotation:
     *      x = x0 + a * X + b * Y
     *      y = y0 + b * X - a * Y
     * where 'X = lng/360' and 'Y = latToNormY(lat)' are normalized from 'lng' and 'lat', and the signs in the second
     * equations are "swapped" because 'Y' grows upwards and 'y' grows downwards.
     * Vertices should have properties 'pos' and 'latLng', with sub-properties 'pos.x', 'pos.y', 'latLng.lat' and 'latLng.lng';
     * vertices for whichTab 'pos' and/or 'latLng' are missing are ignored.
     * If there are no useful vertices, then the current transform is returned. If there is only one data point then the
     * current scale is kept, with a translation so that that data point matches up.
     * If there are more than 2 data points then a least-mean sqares fit is done for the transformation above.
     * @method fitWithRotation
     * @param {Array[Object]} vertices
     * @return {Transform}
     */
    Transform.prototype.fitWithRotation = function (vertices) {
        // >= 1 points => want: scale & rotate:
        /*
            Looking for translation + rotation + scaling: X = lng/360, Y = latToNormY(lat);
            want: x = x0 + a * X + b * Y; y = y0 + b * X - a * Y (translate, scale and rotation, plus y flipped).
            Look for least squares error: min sum (x0 + a* X + b * Y - x)^2 + (y0 +b * X - a * Y - y)^2.
            Least squares: minimise sum ( (lat - ax - by - c)^2 + (lng +b x - ay - d)^2).
            Result: (with N = # points, sX = sum(X), sXx = sum (X * x) etc:
            a = (N sXx - sX sx - N sYy - sY sy) / (N sXX - SX^2 + N sYY - SY^2),
            b = (sXy - sX sY + sYx - sY sx) / (N sXX - SX^2 + N sYY - SY^2),
            x0 = ( sx - a sX - b sY) / N,
            y0 = ( sy - b sX + a sY) / N.
            For the inverse transform: [ X; Y ] = 1/(a^2+b^2) * [ a, b; b, -a] * [x-x0 ; y - y0].
         */
        var x0, y0;
        var sums = calculateSums(vertices);
        if (!sums.N) {
            return this;
        } // should not happen

        var den = sums.N * sums.XX - sums.X * sums.X + sums.N * sums.YY - sums.Y * sums.Y;

        if (sums.N === 1 || den <= 0) {
            // then keep size, only shift such that <X>, <Y> maps to <x>, <y>
            x0 = (sums.x - this.size * sums.X) / sums.N;
            y0 = (sums.y + this.size * sums.Y) / sums.N;
            return new Transform({x0: x0, y0: y0, size: this.size});
        } else { // rotate and scale as well
            var a = ( sums.N * sums.Xx - sums.X * sums.x - sums.N * sums.Yy + sums.Y * sums.y ) / den;
            var b = ( sums.N * sums.Xy - sums.X * sums.y + sums.N * sums.Yx - sums.Y * sums.x ) / den;
            var a2b2 = a * a + b * b;
            if (a2b2 === 0) { // indeterminate => keep size
                a = this.size;
                a2b2 = a * a;
                b = 0;
            }
            x0 = (sums.x - a * sums.X - b * sums.Y) / sums.N;
            y0 = (sums.y - b * sums.X + a * sums.Y) / sums.N;
            var transform = new Transform({
                x0: x0,
                y0: y0,
                size: Math.sqrt(a2b2)
            });
            // and override conversion methods:
            transform.toLatLng = function (point) {
                var x = point.x - x0; // shifted to origin
                var y = point.y - y0;
                var X = (a * x + b * y) / a2b2;
                var Y = (b * x - a * y) / a2b2;
                return L.latLng(normYToLat(Y), X * 360);
            };
            transform.toXY = function (latLng) {
                var X = latLng.lng / 360;
                var Y = latToNormY(latLng.lat);
                return L.point(x0 + a * X + b * Y, y0 + b * X - a * Y);
            };
            return transform;
        }
    };

    /**
     * Calculate the transformation (translation and scaling only) that best matches for the given vertices.
     * Vertices should have properties 'pos' and 'latLng', with sub-properties 'pos.x', 'pos.y', 'latLng.lat' and 'latLng.lng';
     * vertices for whichTab 'pos' and/or 'latLng' are missing are ignored.
     * Without useful data points the default transformation is returned. With a single useful data point the size is set to the default,
     * and the translation is such that that data point lines up. With multiple data points a least-mean-squares fit is done.
     * @method fitWithoutRotation
     * @static
     * @param {Array[Object]} vertices
     * @return {Transform}
     */
    Transform.fitWithoutRotation = function (vertices) {
        var size; // i, latLng, pos
        var sums = calculateSums(vertices);
        if (sums.N === 0) {
            return new Transform();
        }

        /*
         * If we have multiple points, we want the transform such that: x = x0 + size * X + x0 and y = y0 - size * Y
         * Do a least squares fit on
         * Sum ( x - size * X + x0)^2 + Sum ( y - size * Y + y0)^2: size = ( N sXx - sX sx - N sYy + sY sy) / ( N sXX - sX^2 + N sYY - sY^2),
         * x0 = (sx - size * sX) / N , y0 = ( sy + size * sY) / N.
         * That's if we have more than 1 point, with a positive denominator.
         * For a single point we will use the default Leaflet's size at 2^7, so 256 * 2^7 = DEFAULT_SIZE
         */
        var den = sums.N * sums.XX - sums.X * sums.X + sums.N * sums.YY - sums.Y * sums.Y;
        if (sums.N > 1 && den > 0) {
            size = (sums.N * sums.Xx - sums.X * sums.x - sums.N * sums.Yy + sums.Y * sums.y) / den;
            if (size === 0) {
                size = DEFAULT_SIZE;
            } // if indeterminate
        } else {
            size = DEFAULT_SIZE;
        }

        var x0 = (sums.x - size * sums.X) / sums.N;
        var y0 = (sums.y + size * sums.Y) / sums.N;

        return new Transform({x0: x0, y0: y0, size: size});
    };

    /**
     * Create a transform such that the (x,y) 'bounds' fits inside 'latLngBounds'.
     * Possible option are:
     *  - 'minSize': (x, y) width and height are increased to be at least 'minSize'
     *  - 'padding', a fraction of latLngBounds to leave emtpy (half of that on each side)
     *  - 'bigger', whichTab determines the behaviour in case the
     * aspect ratio of the bounds differ:
     * if it is the string 'xy', the (lat,lng) bounds will fit inside the (x,y) bounds;
     * if it is 'latLng' or any other value not they will extend the (x,y) bounds.
     * If 'bounds' is invalid they will be set to ([-10, -10], [800, 500]); if 'latLngBounds' is invalid the default Transform will be returned.
     * @method fitBoundsTransform
     * @static
     * @param {L.Bounds} bounds
     * @param {L.LatLngBounds} latLngBounds
     * @param {Object} options
     * @return {Transform}
     */
    Transform.fitBoundsTransform = function (bounds, latLngBounds, options) {
        options = options || {};
        if (!latLngBounds.isValid()) return new Transform();
        if (!bounds.isValid()) { // no x, y
            bounds = L.bounds([-10, -10], [800, 500]);
        }
        var Ytop = latToNormY(latLngBounds.getNorth());
        var Ybot = latToNormY(latLngBounds.getSouth());
        var Xlft = latLngBounds.getWest() / 360;
        var Xrt = latLngBounds.getEast() / 360;
        var YCollapsed = Ytop <= Ybot;
        var XCollapsed = Xrt <= Xlft;
        if (XCollapsed && YCollapsed || (XCollapsed || YCollapsed) && options.bigger !== 'xy') {
            return new Transform();
        }
        var xySize = bounds.getSize();
        var minSize = options.minSize || 1;
        if (xySize.x < minSize) xySize.x = minSize;
        if (xySize.y < minSize) xySize.y = minSize;
        var invSizeX = (Xrt - Xlft) / xySize.x;
        var invSizeY = (Ytop - Ybot) / xySize.y;
        var invSize;
        if (options.bigger === 'xy') {
            invSize = invSizeX > invSizeY ? invSizeX : invSizeY;
        } else {
            invSize = invSizeX < invSizeY ? invSizeX : invSizeY;
        }
        if (options.padding) {
            invSize = invSize * (1 - options.padding);
        }
        return new Transform({
            xy: bounds.getCenter(),
            latLng: latLngBounds.getCenter(),
            size: 1 / invSize
        });
    };

    return Transform;
});