package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SupportTicket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun getAllTickets(): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets WHERE id = :id")
    fun getTicketById(id: Long): Flow<SupportTicket?>

    @Query("SELECT * FROM support_tickets WHERE id = :id")
    suspend fun getTicketByIdSync(id: Long): SupportTicket?

    @Query("SELECT * FROM support_tickets WHERE channel = :channel ORDER BY updatedAt DESC")
    fun getTicketsByChannel(channel: String): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets WHERE customerPhone = :phone AND status != 'CLOSED' AND status != 'RESOLVED' LIMIT 1")
    suspend fun findOpenTicketByPhone(phone: String): SupportTicket?

    @Query("SELECT COUNT(*) FROM support_tickets")
    suspend fun getTicketCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<SupportTicket>)

    @Update
    suspend fun updateTicket(ticket: SupportTicket)

    @Delete
    suspend fun deleteTicket(ticket: SupportTicket)

    @Query("UPDATE support_tickets SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE support_tickets SET status = :newStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE support_tickets SET priority = :priority, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePriority(id: Long, priority: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE support_tickets SET appointmentDate = :date, appointmentTimeSlot = :slot, assignedTechnician = :tech, status = 'SCHEDULED', updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAppointment(id: Long, date: Long?, slot: String?, tech: String, updatedAt: Long = System.currentTimeMillis())
}
