function ApplicationModel(map, cfg) {
    var self = this;

    self.map = map;
    self.username = "username here";
    self.markers = [];

    self.topicItems = ko.observableArray([]);
    self.topicIds = ko.observable(-1);
    self.sessionItems = ko.observableArray([]);
    self.sessionIds = ko.observable(-1);

    self.init = function() {
        getTopics();

        self.topicIds.subscribe(function(newvalue) {
            getSessions(newvalue);
        });
        self.sessionIds.subscribe(function(newvalue) {
            drawMarkers(newvalue);
        });
    }



    self.refresh = function() {
        deleteMarkers();
        drawMarkers(4);
    }

    function getSessions(topicId) {
        self.sessionItems.removeAll();
        $.ajax({
            url: '/api/sessions/'+topicId,
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    self.sessionItems.push({
                        text: new Date(item.date),
                        id: item.id
                    });
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function getTopics() {
        self.topicItems.removeAll();
        $.ajax({
            url: '/api/topics',
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    self.topicItems.push({
                        text: item.name,
                        id: item.id
                    });
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    //draw all of the markers on the map
    function drawMarkers(trackId) {
        deleteMarkers();
        $.ajax({
            url: '/api/tracks/' + trackId,
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    addMarker(item);
                });
                showMarkers();
            })
            .fail(function() {
                alert("failed.")
            });
    }

    //add a marker from the raw data
    function addMarker(data) {
        var marker = new MarkerWithLabel({

            position: new google.maps.LatLng(data.lat, data.lon),
            //icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png',
            labelContent: 'Lat: ' + data.lat + ' Lng: ' + data.lon,
            //zIndex: zindex,
            labelAnchor: new google.maps.Point(20, 0),
            //labelClass: "labels", // the CSS class for the label
            labelInBackground: false
        });
        markers.push(marker);
    }

    // Sets the map on all markers in the array.
    function setAllMap(map) {
        for (var i = 0; i < markers.length; i++) {
            markers[i].setMap(map);
        }
    }

    // Removes the markers from the map, but keeps them in the array.
    function clearMarkers() {
        setAllMap(null);
    }

    // Shows any markers currently in the array.
    function showMarkers() {
        setAllMap(map);
    }

    // Deletes all markers in the array by removing references to them.
    function deleteMarkers() {
        clearMarkers();
        markers = [];
    }
}