package com.obscuracalc.core.vault.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VaultEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultEntryDao(): VaultEntryDao

    companion object {
        fun build(context: Context): VaultDatabase {
            return Room.databaseBuilder(
                context,
                VaultDatabase::class.java,
                "obscuracalc_vault.db",
            ).fallbackToDestructiveMigration(true).build()
        }
    }
}
