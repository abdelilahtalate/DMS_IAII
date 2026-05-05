import logging
import sys

# Create a custom logger
logger = logging.getLogger("dms")

# Set the default logging level
logger.setLevel(logging.INFO)

# Create handlers (console in this case)
c_handler = logging.StreamHandler(sys.stdout)
c_handler.setLevel(logging.INFO)

# Create formatters and add it to handlers
c_format = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
c_handler.setFormatter(c_format)

# Add handlers to the logger
if not logger.handlers:
    logger.addHandler(c_handler)
