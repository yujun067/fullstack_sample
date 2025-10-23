-- Insert initial feature flags
INSERT INTO feature_flags (name, description, enabled, created_by, updated_by) VALUES
('dark_mode', 'Enable dark theme for the application', false, 'system', 'system'),
('maintenance_mode', 'Enable maintenance mode to show maintenance page', false, 'system', 'system'),
('new_search_ui', 'Enable new search UI design', true, 'system', 'system'),
('advanced_filtering', 'Enable advanced filtering options', false, 'system', 'system');
