def get_scores(filename)
  # Default score.
  return [] unless File.exists?(filename)
  results = []
  File.open(filename) do |file|
    file.read.scan(/Player #0's happiness is: (.*)\n/) { |matches|
      results << Float(matches[0])
    }
  end
  results
end

root_path = 'scripts/output/'
threshold = 0.9

Dir.foreach(root_path) do |path|
  next if path.include?('old_') or path == '.' or path == '..'

  old_scores = get_scores("#{root_path}old_#{path}")

  # No old data.
  if old_scores.length == 0
    exit
  end

  new_scores = get_scores(root_path + path)

  if old_scores.length != new_scores.length
    puts "old scores and new scores don't align"
    exit 1
  end

  failed = false
  0.upto(old_scores.length - 1).each do |i|
    old_score = old_scores[i]
    new_score = new_scores[i]
    # Want scores to increase. Check for regression
    if (new_score * 1.0 / old_score < threshold && old_score > new_score) ||
        new_score < 0
      puts "Possible regression detected for case #{i}!"
      puts "Old data: #{old_score}"
      puts "New data: #{new_score}"
      puts ""
      failed = true
    end
  end

  exit (failed ? 1 : 0)

end

