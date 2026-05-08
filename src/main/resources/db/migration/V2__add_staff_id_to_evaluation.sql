-- =====================================================
-- Migration V2: Add staff_id to evaluation table
-- Purpose: Fix cross-branch data isolation issue
-- Author: System
-- Date: 2026-05-08
-- =====================================================

-- Step 1: Add staff_id column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'evaluation' AND column_name = 'staff_id') THEN
        ALTER TABLE evaluation ADD COLUMN staff_id BIGINT;
        RAISE NOTICE 'Added staff_id column to evaluation table';
    ELSE
        RAISE NOTICE 'staff_id column already exists';
    END IF;
END $$;

-- Step 2: Populate staff_id from task_assignment for existing evaluations
UPDATE evaluation e
SET staff_id = ta.staff_id
FROM task_assignment ta
WHERE ta.task_id = e.task_id 
  AND e.staff_id IS NULL;

-- Step 3: Make staff_id NOT NULL (only if no nulls remain)
DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count FROM evaluation WHERE staff_id IS NULL;
    IF null_count = 0 THEN
        ALTER TABLE evaluation ALTER COLUMN staff_id SET NOT NULL;
        RAISE NOTICE 'Set staff_id as NOT NULL';
    ELSE
        RAISE NOTICE 'Warning: % NULL staff_id values remain. Please investigate.', null_count;
    END IF;
END $$;

-- Step 4: Add foreign key constraint
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'fk_evaluation_staff') THEN
        ALTER TABLE evaluation ADD CONSTRAINT fk_evaluation_staff 
            FOREIGN KEY (staff_id) REFERENCES users(staff_id) ON DELETE CASCADE;
        RAISE NOTICE 'Added foreign key constraint fk_evaluation_staff';
    END IF;
END $$;

-- Step 5: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_evaluation_staff_id ON evaluation(staff_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_staff_task ON evaluation(staff_id, task_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_staff_quarter ON evaluation(staff_id, year, quarter);
CREATE INDEX IF NOT EXISTS idx_evaluation_staff_year_quarter ON evaluation(staff_id, year, quarter);

-- Step 6: Update statistics for query optimizer
ANALYZE evaluation;

-- Verification query
SELECT '✅ Migration V2 completed successfully!' as status;
SELECT 
    COUNT(*) as total_evaluations,
    COUNT(DISTINCT staff_id) as unique_staff_with_evaluations,
    MIN(year) as earliest_year,
    MAX(year) as latest_year
FROM evaluation;
