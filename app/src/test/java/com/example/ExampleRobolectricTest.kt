package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ItineraryDao
import com.example.data.local.ItineraryStopEntity
import com.example.data.local.OrderedItineraryStop
import com.example.data.local.PlaceDao
import com.example.data.local.PlaceEntity
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.model.AccessibilityLevel
import com.example.data.model.Place
import com.example.data.repository.PlacesRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var placeDao: PlaceDao
    private lateinit var itineraryDao: ItineraryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        placeDao = db.placeDao()
        itineraryDao = db.itineraryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Prayagraj Sangam Yatra", appName)
    }

    @Test
    fun `insert and retrieve places from Room database`() = runBlocking {
        val testPlace = Place(
            id = "test-sangam-01",
            name = "Triveni Sangam",
            hindiName = "त्रिवेणी संगम",
            category = "ghat",
            description = "Sacred confluence",
            latitude = 25.4290,
            longitude = 81.8845,
            accessibilityLevel = AccessibilityLevel.EASY,
            tags = listOf("Holy Dip", "Sangam")
        )

        placeDao.insertPlace(testPlace.toEntity())

        val retrieved = placeDao.getPlaceById("test-sangam-01")
        assertNotNull(retrieved)
        assertEquals("Triveni Sangam", retrieved?.name)
        assertEquals("ghat", retrieved?.category)
        assertEquals(listOf("Holy Dip", "Sangam"), retrieved?.tags)
    }

    @Test
    fun `placeDao insertAll getAllPlaces and deleteAll manage cache properly`() = runBlocking {
        placeDao.deleteAll()
        assertTrue(placeDao.getAllPlacesList().isEmpty())

        val list = listOf(
            Place(
                id = "place-1",
                name = "Place One",
                category = "temple",
                description = "Desc 1",
                latitude = 25.4,
                longitude = 81.8
            ).toEntity(),
            Place(
                id = "place-2",
                name = "Place Two",
                category = "ghat",
                description = "Desc 2",
                latitude = 25.5,
                longitude = 81.9
            ).toEntity()
        )

        placeDao.insertPlaces(list)
        val fetchedFlow = placeDao.getAllPlaces().first()
        assertEquals(2, fetchedFlow.size)

        val fetchedList = placeDao.getAllPlacesList()
        assertEquals(2, fetchedList.size)

        placeDao.deleteAll()
        val afterDelete = placeDao.getAllPlacesList()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `placeDao selection state and itinerary stops query operate correctly`() = runBlocking {
        val list = listOf(
            PlaceEntity(
                id = "p-1",
                name = "Sangam",
                category = "ghat",
                description = "Confluence",
                latitude = 25.42,
                longitude = 81.88,
                isSelected = false
            ),
            PlaceEntity(
                id = "p-2",
                name = "Hanuman Temple",
                category = "temple",
                description = "Let Hanuman Ji",
                latitude = 25.43,
                longitude = 81.89,
                isSelected = true
            )
        )
        placeDao.insertPlaces(list)

        var selectedStops = placeDao.getItineraryStops().first()
        assertEquals(1, selectedStops.size)
        assertEquals("p-2", selectedStops[0].id)

        placeDao.updateSelectionState("p-1", true)
        selectedStops = placeDao.getItineraryStops().first()
        assertEquals(2, selectedStops.size)

        placeDao.clearAllSelections()
        selectedStops = placeDao.getItineraryStops().first()
        assertTrue(selectedStops.isEmpty())
    }

    @Test
    fun `itineraryDao performs CRUD and sequence ordering on custom itinerary stops`() = runBlocking {
        val place1 = PlaceEntity(
            id = "p-1",
            name = "Sangam Holy Dip",
            category = "ghat",
            description = "Sacred confluence",
            latitude = 25.4290,
            longitude = 81.8845
        )
        val place2 = PlaceEntity(
            id = "p-2",
            name = "Bade Hanuman Darshan",
            category = "temple",
            description = "Temple of reclining Hanuman Ji",
            latitude = 25.4320,
            longitude = 81.8900
        )
        placeDao.insertPlaces(listOf(place1, place2))

        val stop1 = ItineraryStopEntity(
            placeId = "p-1",
            sequenceOrder = 0
        )
        val stop2 = ItineraryStopEntity(
            placeId = "p-2",
            sequenceOrder = 1
        )

        itineraryDao.insertAllStops(listOf(stop1, stop2))
        val orderedStops = itineraryDao.getOrderedItinerary().first()
        assertEquals(2, orderedStops.size)
        assertEquals("p-1", orderedStops[0].place.id)
        assertEquals(0, orderedStops[0].sequenceOrder)
        assertEquals("Sangam Holy Dip", orderedStops[0].place.name)
        assertEquals("p-2", orderedStops[1].place.id)
        assertEquals(1, orderedStops[1].sequenceOrder)

        // Reorder stops
        itineraryDao.insertStop(ItineraryStopEntity(placeId = "p-1", sequenceOrder = 2))
        val reorderedStops = itineraryDao.getOrderedItinerary().first()
        assertEquals("p-2", reorderedStops[0].place.id)
        assertEquals("p-1", reorderedStops[1].place.id)

        // Remove a stop
        itineraryDao.removeStop("p-2")
        val remaining = itineraryDao.getOrderedItinerary().first()
        assertEquals(1, remaining.size)
        assertEquals("p-1", remaining[0].place.id)

        // Clear itinerary
        itineraryDao.clearItinerary()
        assertEquals(0, itineraryDao.getStopCount())
    }

    @Test
    fun `places repository returns cached places when offline`() = runBlocking {
        val repository = PlacesRepositoryImpl(
            customDao = placeDao
        )

        // Fetch places (offline mode)
        val result = repository.getPlaces(category = null)
        assertTrue(result.isSuccess)
        val places = result.getOrNull()
        assertNotNull(places)
        assertTrue(places!!.isNotEmpty())

        // Verify Room DB count has been seeded and cached
        val count = placeDao.getPlacesCount()
        assertTrue(count > 0)
    }

    @Test
    fun `places viewmodel updates place star ratings correctly`() {
        val repository = PlacesRepositoryImpl(customDao = placeDao)
        val viewModel = com.example.ui.viewmodel.PlacesViewModel(repository = repository)
        val testPlaceId = "11111111-1111-1111-1111-111111111101"

        // Initially no rating
        assertEquals(0, viewModel.placeRatings.value[testPlaceId] ?: 0)

        // Set 5 star rating
        viewModel.setPlaceRating(testPlaceId, 5)
        assertEquals(5, viewModel.placeRatings.value[testPlaceId])

        // Update to 4 star rating
        viewModel.setPlaceRating(testPlaceId, 4)
        assertEquals(4, viewModel.placeRatings.value[testPlaceId])

        // Reset rating with 0
        viewModel.setPlaceRating(testPlaceId, 0)
        assertEquals(0, viewModel.placeRatings.value[testPlaceId] ?: 0)
    }

    @Test
    fun `directions repository returns empty list when less than two places`() = runBlocking {
        val directionsRepo = com.example.data.repository.DirectionsRepository(apiKey = "dummy_key")
        val singlePlaceList = listOf(
            Place(
                id = "1",
                name = "Sangam",
                category = "ghat",
                latitude = 25.4290,
                longitude = 81.8845
            )
        )
        val routePoints = directionsRepo.getRoadRoutePoints(singlePlaceList)
        assertTrue(routePoints.isEmpty())
    }

    @Test
    fun `baseline average rating produces valid range for place cards`() {
        val (avg, count) = com.example.ui.screens.getBaselineAverageRating("11111111-1111-1111-1111-111111111101")
        assertTrue("Average rating should be between 4.0 and 5.0", avg in 4.0..5.0)
        assertTrue("Rating count should be greater than 0", count > 0)
    }

    @Test
    fun `database module provides database, dao and repository instances`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = com.example.data.local.DatabaseModule.provideDatabase(context)
        assertNotNull(database)

        val placeDao = com.example.data.local.DatabaseModule.providePlaceDao(context)
        assertNotNull(placeDao)

        val itineraryDao = com.example.data.local.DatabaseModule.provideItineraryDao(context)
        assertNotNull(itineraryDao)

        val repository = com.example.data.local.DatabaseModule.providePlacesRepository(context)
        assertNotNull(repository)
    }

    @Test
    fun `prayagraj database singleton creates database and provides placeDao and itineraryDao`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.PrayagrajDatabase.getDatabase(context)
        assertNotNull(db)
        val placeDao = db.placeDao()
        assertNotNull(placeDao)
        val itineraryDao = db.itineraryDao()
        assertNotNull(itineraryDao)
    }

    @Test
    fun `persisted itinerary repository handles adding, moving, and reindexing stops`() = runBlocking {
        val place1 = PlaceEntity(
            id = "stop-a",
            name = "Triveni Sangam",
            category = "ghat",
            description = "Confluence of rivers",
            latitude = 25.429,
            longitude = 81.884
        )
        val place2 = PlaceEntity(
            id = "stop-b",
            name = "Allahabad Fort",
            category = "heritage",
            description = "Akbar Fort",
            latitude = 25.431,
            longitude = 81.876
        )
        val place3 = PlaceEntity(
            id = "stop-c",
            name = "Anand Bhavan",
            category = "museum",
            description = "Nehru ancestral home",
            latitude = 25.456,
            longitude = 81.859
        )
        placeDao.insertPlaces(listOf(place1, place2, place3))

        // Use in-memory db backed repository
        val mockDb = db as? com.example.data.local.PrayagrajDatabase
        val testRepo = com.example.data.repository.PersistedItineraryRepository(
            database = com.example.data.local.PrayagrajDatabase.getDatabase(ApplicationProvider.getApplicationContext()),
            itineraryDao = itineraryDao
        )

        testRepo.addStop("stop-a")
        testRepo.addStop("stop-b")
        testRepo.addStop("stop-c")

        var stops = testRepo.orderedItinerary.first()
        assertEquals(3, stops.size)
        assertEquals("stop-a", stops[0].id)
        assertEquals("stop-b", stops[1].id)
        assertEquals("stop-c", stops[2].id)

        // Move stop-c up
        testRepo.moveStopUp(stops, 2)
        stops = testRepo.orderedItinerary.first()
        assertEquals("stop-c", stops[1].id)
        assertEquals("stop-b", stops[2].id)

        // Move stop-a down
        testRepo.moveStopDown(stops, 0)
        stops = testRepo.orderedItinerary.first()
        assertEquals("stop-c", stops[0].id)
        assertEquals("stop-a", stops[1].id)
        assertEquals("stop-b", stops[2].id)

        // Remove stop-a and verify reindexing
        testRepo.removeStop("stop-a")
        stops = testRepo.orderedItinerary.first()
        assertEquals(2, stops.size)
        assertEquals("stop-c", stops[0].id)
        assertEquals("stop-b", stops[1].id)
    }

    @Test
    fun `network connectivity observer emits initial network status`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val observer = com.example.data.remote.NetworkConnectivityObserver(context)
        val initialStatus = observer.observe().first()
        assertNotNull(initialStatus)
    }

    @Test
    fun `placesViewModel observes network status and syncs remote data on available`() = runBlocking {
        val mockConnectivity = object : com.example.data.remote.ConnectivityObserver {
            override fun observe(): kotlinx.coroutines.flow.Flow<com.example.data.remote.NetworkStatus> {
                return kotlinx.coroutines.flow.flowOf(
                    com.example.data.remote.NetworkStatus.Unavailable,
                    com.example.data.remote.NetworkStatus.Available
                )
            }
        }
        val customRepo = com.example.data.repository.PlacesRepositoryImpl(customDao = placeDao)
        val viewModel = com.example.ui.viewmodel.PlacesViewModel(
            repository = customRepo,
            connectivityObserver = mockConnectivity
        )

        val status = viewModel.networkStatus.first()
        assertNotNull(status)
    }

    @Test
    fun `panda repository returns all verified pandas`() = runBlocking {
        val pandaRepo = com.example.data.repository.PandaRepositoryImpl()
        val allPandas = pandaRepo.getAllPandas().first()
        assertTrue("Pandas list should not be empty", allPandas.isNotEmpty())
        assertTrue("All pandas should have valid accreditation", allPandas.all { it.accreditationId.startsWith("PRY-") })
        assertTrue("All pandas should have services list", allPandas.all { it.services.isNotEmpty() })
    }

    @Test
    fun `panda repository filters by search query and ritual category`() = runBlocking {
        val pandaRepo = com.example.data.repository.PandaRepositoryImpl()
        
        // Search by name
        val ramakantResults = pandaRepo.filterPandas(query = "Ramakant").first()
        assertEquals(1, ramakantResults.size)
        assertEquals("panda-01", ramakantResults[0].id)

        // Filter by ritual category (Pind Daan)
        val pindDaanPandas = pandaRepo.filterPandas(ritualCategory = "Pind Daan & Tarpan").first()
        assertTrue(pindDaanPandas.isNotEmpty())
        assertTrue(pindDaanPandas.all { p -> p.services.any { it.category == "Pind Daan & Tarpan" } })

        // Filter by language (Bengali)
        val bengaliPandas = pandaRepo.filterPandas(language = "Bengali").first()
        assertTrue(bengaliPandas.isNotEmpty())
        assertTrue(bengaliPandas.all { it.languages.contains("Bengali") })
    }

    @Test
    fun `panda repository filters by ghat and bahi-khata availability`() = runBlocking {
        val pandaRepo = com.example.data.repository.PandaRepositoryImpl()

        // Filter by Arail ghat
        val arailPandas = pandaRepo.filterPandas(ghatCategory = "arail").first()
        assertEquals(1, arailPandas.size)
        assertEquals("panda-05", arailPandas[0].id)

        // Filter by Bahi-Khata only
        val bahiKhataPandas = pandaRepo.filterPandas(bahiKhataOnly = true).first()
        assertTrue(bahiKhataPandas.isNotEmpty())
        assertTrue(bahiKhataPandas.all { it.bahiKhataAvailable })
    }

    @Test
    fun `panda viewModel manages search query, filter changes, and booking flow`() = runBlocking {
        val pandaRepo = com.example.data.repository.PandaRepositoryImpl()
        val viewModel = com.example.ui.viewmodel.PandaViewModel(repository = pandaRepo)

        val initialState = viewModel.uiState.value
        assertNotNull(initialState)

        // Update search query
        viewModel.updateSearchQuery("Devendra")
        var state = viewModel.uiState.value
        assertEquals("Devendra", state.searchQuery)

        // Select ritual category
        viewModel.selectRitualCategory("Rudrabhishek")
        state = viewModel.uiState.value
        assertEquals("Rudrabhishek", state.selectedRitualCategory)

        // Clear filters
        viewModel.clearFilters()
        state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals("All Rituals", state.selectedRitualCategory)

        // Start and confirm booking flow
        val panda = pandaRepo.getPandaById("panda-01")
        assertNotNull(panda)
        val service = panda!!.services.first()

        viewModel.startBooking(panda, service)
        state = viewModel.uiState.value
        assertEquals(panda.id, state.bookingPanda?.id)
        assertEquals(service.id, state.selectedServiceForBooking?.id)

        viewModel.confirmBooking(
            panda = panda,
            service = service,
            bookingDate = "Today Morning",
            timeSlot = "06:00 AM",
            devoteeCount = 4,
            notes = "Kashyap Gotra"
        )
        state = viewModel.uiState.value
        assertNotNull(state.lastConfirmedBookingCode)
        assertTrue(state.lastConfirmedBookingCode!!.startsWith("PRY-PUROHIT-"))

        viewModel.dismissSuccessConfirmation()
        state = viewModel.uiState.value
        assertEquals(null, state.lastConfirmedBookingCode)
    }

    @Test
    fun `ai guide repository generates transparent guidance with fair dakshina rules`() = runBlocking {
        val aiRepo = com.example.data.repository.PilgrimageAiGuideRepositoryImpl()
        
        // Query about Sankalp fair rates
        val sankalpResponse = aiRepo.getAiResponse(
            userMessage = "How much Dakshina should I pay for Sankalp puja at Sangam?",
            history = emptyList()
        )
        assertNotNull(sankalpResponse)
        assertTrue("Response should contain text", sankalpResponse.text.isNotEmpty())
        assertTrue("Response should contain Sankalp advice", sankalpResponse.text.contains("Sankalp", ignoreCase = true))
        assertTrue("Response should mention fair dakshina range", sankalpResponse.text.contains("101") || sankalpResponse.text.contains("₹"))

        // Query about Pind Daan rates and Panda verification
        val pindDaanResponse = aiRepo.getAiResponse(
            userMessage = "What is the official rate for Pind Daan and how to verify Panda?",
            history = emptyList()
        )
        assertNotNull(pindDaanResponse)
        assertTrue("Response should mention Pind Daan", pindDaanResponse.text.contains("Pind Daan", ignoreCase = true))
        assertTrue("Response should mention verification or accreditation", pindDaanResponse.text.contains("badge", ignoreCase = true) || pindDaanResponse.text.contains("verified", ignoreCase = true) || pindDaanResponse.text.contains("PRY-PUROHIT", ignoreCase = true))
    }

    @Test
    fun `ai guide viewModel handles message send, clear chat, and quick prompts`() = runBlocking {
        val aiRepo = com.example.data.repository.PilgrimageAiGuideRepositoryImpl()
        val viewModel = com.example.ui.viewmodel.AiGuideViewModel(repository = aiRepo)

        // Verify initial greeting message exists
        val initialState = viewModel.uiState.value
        assertFalse(initialState.messages.isEmpty())
        assertEquals(com.example.data.model.MessageSender.AI, initialState.messages[0].sender)

        // Send a message
        viewModel.onInputTextChanged("What is fair dakshina for Rudrabhishek?")
        assertEquals("What is fair dakshina for Rudrabhishek?", viewModel.uiState.value.inputText)
        viewModel.sendMessage()

        var state = viewModel.uiState.value
        assertTrue("Input should be cleared after sending", state.inputText.isEmpty())
        assertTrue("Messages count should increase", state.messages.size >= 2)

        // Toggle tariffs sheet
        viewModel.toggleTariffSheet(true)
        assertTrue(viewModel.uiState.value.showTariffSheet)
        viewModel.toggleTariffSheet(false)
        assertFalse(viewModel.uiState.value.showTariffSheet)

        // Clear chat
        viewModel.clearChat()
        state = viewModel.uiState.value
        assertEquals(1, state.messages.size) // Only initial greeting remains
    }
}
