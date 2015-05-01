var Api = (function()
{
    var pub = {},
    //Private property
    greyFloorTile = null;

    //Public property
    //pub.ingredient = "Bacon Strips";

    //Public method
    pub.getAllItems = function(callback)
    {
    console.log("Calling");
        $.getJSON( "api/feeds/users/RichoDemus/feeds", callback);
    };

    pub.markAsRead = function(feedId, itemId)
    {
        console.log("Marking item " + itemId + " in feed " + feedId + "  as read");
        jQuery.ajax ({
            url: "api/feeds/users/RichoDemus/feeds/" + feedId + "/items/" + itemId + "/",
            type: "POST",
            data: JSON.stringify({ action: "MARK_READ" }),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            success: function(){
                //
            }
        });
    }

    pub.markAsUnread = function(feedId, itemId)
    {
        console.log("Marking item " + itemId + " in feed " + feedId + " as unread");
        jQuery.ajax ({
            url: "api/feeds/users/RichoDemus/feeds/" + feedId + "/items/" + itemId + "/",
            type: "POST",
            data: JSON.stringify({ action: "MARK_UNREAD" }),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            success: function(){
                //
            }
        });
    }

    //Private method
    /*
    function privateWay() {
        console.log("private method");
    }
	*/
    //Return just the public parts
    return pub;

}());