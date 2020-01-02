---@param s string
---@param l number
---@param c string
---@return string
function string.addPrefix(s, l, c)
    local sLen = #s
    if (sLen < l) then
        return string.rep(c, l - sLen) .. s
    end
    return s
end

---@param pack table
---@param depth number
function showPackage(pack, depth)
    local result = ''
    local prefix = string.rep('    ', depth)
    local maxKLen = 0
    local maxVLen1 = 0
    local maxVLen2 = 0
    for k, v in pairs(pack) do
        local kLen = #k
        local vStr = tostring(v)
        if (maxKLen < kLen) then
            maxKLen = kLen
        end
        local startIndex, endIndex, matchStr = vStr:find(':')
        if (startIndex ~= nil) then
            local vLen1 = #(vStr:sub(1, startIndex - 1))
            local vLen2 = #(vStr:sub(endIndex + 1))
            if (maxVLen1 < vLen1) then
                maxVLen1 = vLen1
            end
            if (maxVLen2 < vLen2) then
                maxVLen2 = vLen2
            end
        end
    end
    for k, v in pairs(pack) do
        local vStr = tostring(v)
        local startIndex, endIndex, matchStr = vStr:find(':')
        if (startIndex ~= nil) then
            vStr = vStr:sub(1, startIndex - 1):addPrefix(maxVLen1, ' ') .. vStr:sub(endIndex + 1):addPrefix(maxVLen2, ' ')
        else
            vStr = vStr:addPrefix(maxVLen1, ' ')
        end
        result = result .. prefix .. string.addPrefix(k, maxKLen, ' ') .. ' -- ' .. vStr .. '\n'
        if (type(v) == 'table') then
            result = result .. '\n' .. showPackage(v, depth + 1)
        end
    end
    return result
end

print(showPackage(_G, 0))
