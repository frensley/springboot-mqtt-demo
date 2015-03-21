function ApplicationModel(map, cfg) {
    var self = this;

    self.map = map;
    self.username = "username here";
    self.markers = [];
    self.trackLine = undefined;

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
            //drawMarkers(newvalue);
            drawTracks(newvalue);
            drawChart($('#chart1')[0], newvalue)
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

    //xhr to get paths for trackline and then draw
    function drawTracks(trackId) {
        removeTrackLine(self.trackLine);
        var path = [];
        $.ajax({
            url: '/api/tracks/' + trackId,
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    path.push(new google.maps.LatLng(item.lat,item.lon));
                });
                drawTrackLine(path);
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
                drawTrackLine(path)
            })
            .fail(function() {
                alert("failed.")
            });
    }

    //draw trackline on map
    function drawTrackLine(path) {
        self.trackLine  = new google.maps.Polyline({
            path: path,
            strokeColor: 'green',
            strokeWeight: 5,
            strokeOpacity:.65
        });
        self.trackLine.setMap(self.map);
    }

    //remove track line from map
    function removeTrackLine(path) {
        if (self.trackLine) {
            self.trackLine.setMap(null);
        }
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
            labelInBackground: false,
            _id: data.id
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

    /**
     * Draw a chart plotting speed, time, altitude
     * @param el
     * @param trackId
     */

    function drawChart(el, trackId) {
        // Create and populate the data table.
        //var data = google.visualization.arrayToDataTable([
        //    [new Date(2014, 0), 'Data 1', 'Data 2'],
        //    [new Date(2014, 1),   1,       1] ,
        //    [new Date(2014, 2),   2,       0.5],
        //    [new Date(2014, 3),   4,       1],
        //    [new Date(2014, 4),   8,       0.5],
        //    [new Date(2014, 5),   7,       1],
        //    [new Date(2014, 6),   7,       0.5],
        //    [new Date(2014, 7),   8,       1],
        //    [new Date(2014, 8),   4,       0.5],
        //    [new Date(2014, 9),   2,       1],
        //    [new Date(2014, 10),   3.5,     0.5],
        //    [new Date(2014, 11),   3,       1],
        //    [new Date(2014, 12),   3.5,     0.5]
        //]);

        var dataTable = new google.visualization.DataTable();
        dataTable.addColumn('number', 'Date');
        dataTable.addColumn('number','Speed');
        dataTable.addColumn('number','Altitude');

        $.ajax({
            url: '/api/tracks/chart/' + trackId,
            dataType: 'json'
        })
            .done(function(data) {
                dataTable.addRows(data);


                // Create and draw the visualization.
                new google.visualization.LineChart(el).
                    draw(dataTable, {vAxes:[
                        {title: 'Speed km/h', titleTextStyle: {color: 'black'}, maxValue: 10}, // Left axis
                        {title: 'Alititude m', titleTextStyle: {color: 'black'}, maxValue: 20} // Right axis
                    ],series:[
                        {targetAxisIndex:0},
                        {targetAxisIndex:1}

                    ]} );
            })
            .fail(function() {
                alert("failed.")
            });
    }
}