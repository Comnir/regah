module.exports.truncate =
    function (string, maxLength) {
        return string.substring(0, Math.min(maxLength, string.length));
    };
