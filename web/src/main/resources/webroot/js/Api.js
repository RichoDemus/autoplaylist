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
        $.getJSON( "api/feed/", callback);
    };

    pub.markAsRead = function(itemId)
    {
        console.log("Marking item " + itemId + " as read");
        jQuery.ajax ({
            url: "api/feed/",
            type: "POST",
            data: JSON.stringify({ id: itemId, action: "MARK_READ" }),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            success: function(){
                //
            }
        });
    }

    pub.markAsUnread = function(itemId)
    {
        console.log("Marking item " + itemId + " as unread");
        jQuery.ajax ({
            url: "api/feed/",
            type: "POST",
            data: JSON.stringify({ id: itemId, action: "MARK_UNREAD" }),
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