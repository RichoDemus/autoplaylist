var Table = ((()=>
{
    var pub = {},
    //Private property
    itemListTableSelector = $('#itemListTable');

    //Public property
    //pub.ingredient = "Bacon Strips";

    //Public method
    pub.init = () =>
    {
        itemListTableSelector.DataTable({
            "paging": true,
            "searching": false,
            "ordering": true,
            "autoWidth": true,
            "order": [[3, "desc"]]
        });
        itemListTableSelector.on('order.dt', ()=>
        {
            const order = itemListTableSelector.DataTable().order();
            Persistence.storeSortOrder(order);
        });
        const order = Persistence.loadSortOrder();
        if (order) {
            itemListTableSelector.DataTable().order(order).draw();
        }
    };

    pub.addFeedToTable = feed=>
    {
        const count = feed.availableItems();
        if (feed.id === selectedFeed) {
            $('#feedListTable').find('> tbody:last')
                .append("<tr id=\"" + feed.id + "\" data-feed-id=\"" + feed.id + "\" onclick=\"Buttons.filterFeedButtonClicked(this)\"><td bgcolor=\"#FF0000\">" + feed.name + "(" + count + ")</td></tr>");
        }
        else {
            $('#feedListTable').find('> tbody:last')
                .append("<tr id=\"" + feed.id + "\" data-feed-id=\"" + feed.id + "\" onclick=\"Buttons.filterFeedButtonClicked(this)\"><td>" + feed.name + "(" + count + ")</td></tr>");
        }
    };

    pub.addItemsToTable = ()=>
    {
        if (!selectedFeed) {
            console.log("No feed selected");
            return;
        }
        feeds.forEach(feed=>
        {
            if (feed.id === selectedFeed) {
                feed.items.forEach(item=>addItemToTable(feed.id, item));
                itemListTableSelector.DataTable().draw();
            }
        });
    };

    function addItemToTable (feedId, item)
    {
        itemListTableSelector.DataTable().row.add([
            Table.getMarkAsReadToggleButton(feedId, item),
            Table.getTitle(feedId, item),
            item.description.substring(0, 10),
            item.uploadDate,
            item.duration,
            item.views,
            getMarkOlderItemsAsReadButton(feedId, item)
        ]);
    }

    pub.addFeedsToTable = ()=>feeds.forEach(feed=>
    {
        if (selectedLabel && selectedLabel.id === UNLABELED_LABEL.id) {
            //add feed if it doesnt belong in any label
            var hasLabel = false;
            for (var i = 0; i < labels.length; i++) {
                var label = labels[i];
                console.log(label);
                if (label.feeds.indexOf(feed.id) > -1) {
                    //this feed belong to a label
                    console.log("Feed [" + feed.name + "] is in label [" + label.name + "]");
                    hasLabel = true;
                    break;
                }
            }
            if (!hasLabel) {
                console.log("Feed [" + feed.name + "] is not in any label");
                Table.addFeedToTable(feed);
            }
        }
        else if ((selectedLabel && selectedLabel.feeds.indexOf(feed.id) > -1) || !selectedLabel) {
            Table.addFeedToTable(feed);
        }
        else if (selectedLabel && selectedLabel === ALL_LABEL) {
            Table.addFeedToTable(feed);
        }
    });

    pub.clearTables = ()=>
    {
        $("#feedListTable").find("tbody tr").remove();
        $("#labelListTable").find("tbody tr").remove();
        itemListTableSelector.dataTable().fnClearTable();
    };

    pub.addLabelToTable = label=>
    {
        const numberOfItemsInLabel = getNumberOfItemsInLabel(label);
        if (selectedLabel && label.id === selectedLabel.id) {
            $('#labelListTable').find('> tbody:last')
                .append("<tr data-label-id=\"" + label.id + "\" onclick=\"Buttons.labelClicked(this)\"><td bgcolor=\"#FF0000\">" + label.name + "(" + numberOfItemsInLabel + ")</td></tr>");
        }
        else {
            $('#labelListTable').find('> tbody:last')
                .append("<tr data-label-id=\"" + label.id + "\" onclick=\"Buttons.labelClicked(this)\"><td>" + label.name + "(" + numberOfItemsInLabel + ")</td></tr>");
        }
    };

    pub.addLabelsToTable = ()=>labels.forEach(label=>Table.addLabelToTable(label));

    pub.getMarkAsReadToggleButton = (feedId, item)=>"<button id=\"markItemAsReadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id +
    "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"Buttons.markAsReadButtonPressed(this)\">Mark as read</button>" +
    "<button id=\"markItemAsUnreadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id +
    "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"Buttons.markAsUnreadButtonPressed(this)\" style=\"display:none;\" >Mark as unread</button>";

    pub.getTitle = (feedId, item)=>"<a id=\"" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id + "\" href=\"" + item.url +
    "\" target=\"_blank\" onClick=\"Buttons.itemTitleClicked(this)\">" + item.title + "</a>";

    //Private method
    function getMarkOlderItemsAsReadButton(feedId, item)
    {
        return "<button id=\"markAllOlderItemsAsReadButton-" + item.id + "\" data-feed-id=\"" + feedId + "\" data-id=\"" + item.id +
            "\" type=\"button\" class=\"btn btn-xs btn-default\" onClick=\"Buttons.markOlderItemsAsReadButtonPressed(this)\">Mark older as read</button>"
    }

    function getNumberOfItemsInLabel(label)
    {
        return label.feeds.map(f => feedIdToCount(f)).reduce((a, b) => a + b, 0);
    }

    function feedIdToCount(feedId)
    {
        return feeds.filter(f => f.id === feedId).map(f => f.availableItems()).reduce((a, b) => a + b, 0);
    }

    return pub;
})());
